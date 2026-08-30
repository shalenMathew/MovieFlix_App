package com.shalenmathew.movieflix.core.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.databinding.LayoutQuickActionItemBinding
import kotlin.math.cos
import kotlin.math.sin

class QuickActionOverlay(private val context: Context) {

    private var overlayView: FrameLayout? = null
    private var ghostImageView: ImageView? = null
    private val buttons = mutableListOf<View>()
    private var isDismissing = false

    interface QuickActionCallback {
        fun onShare()
        fun onRemove()
        fun onCollection()
        fun onChangePoster()
        fun onDismiss()
    }

    fun show(
        parent: ViewGroup,
        targetView: View,
        isTV: Boolean,
        callback: QuickActionCallback
    ) {
        // 1. Create Overlay
        overlayView = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(context, R.color.black_80))
            // Ensure it's above everything including Bottom Navigation
            elevation = context.resources.getDimension(com.intuit.sdp.R.dimen._100sdp)
            setOnClickListener { 
                dismiss()
                callback.onDismiss()
            }
        }

        // 2. Create Ghost Image
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        
        // Adjust for status bar if necessary, but usually getLocationOnScreen is global
        val ghostBitmap = createBitmapFromView(targetView)
        ghostImageView = ImageView(context).apply {
            setImageBitmap(ghostBitmap)
            layoutParams = FrameLayout.LayoutParams(targetView.width, targetView.height)
            x = location[0].toFloat()
            y = location[1].toFloat()
        }

        overlayView?.addView(ghostImageView)
        
        // 3. Create Actions
        val screenWidth = context.resources.displayMetrics.widthPixels
        val centerX = location[0] + targetView.width / 2
        val centerY = location[1] + targetView.height / 2
        val isOnRightSide = centerX > screenWidth / 2
        
        setupActions(centerX, centerY, isOnRightSide, isTV, callback)

        parent.addView(overlayView)
        overlayView?.bringToFront()

        // 4. Animate everything
        animateEntry()
    }

    private fun setupActions(
        centerX: Int, 
        centerY: Int, 
        isOnRightSide: Boolean,
        isTV: Boolean, 
        callback: QuickActionCallback
    ) {
        val actions = mutableListOf<ActionInfo>()
        actions.add(ActionInfo(R.drawable.baseline_share_24, context.getString(R.string.share).plus(" Movie")) { callback.onShare() })
        actions.add(ActionInfo(R.drawable.baseline_delete_24, context.getString(if (isTV) R.string.btn_remove_from_favorites else R.string.btn_remove_from_favorites)) { callback.onRemove() })
        actions.add(ActionInfo(R.drawable.baseline_add_circle_24, context.getString(R.string.add_to_collection)) { callback.onCollection() })
        actions.add(ActionInfo(R.drawable.ic_gallery, context.getString(if (isTV) R.string.btn_change_show_poster else R.string.btn_change_movie_poster)) { callback.onChangePoster() })

        val radius = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._95sdp).toFloat()
        
        // Start exactly at 12 o'clock (-90 degrees)
        val startAngle = -90.0
        val angleStep = 55.0

        actions.forEachIndexed { index, action ->
            val angle = Math.toRadians(if (isOnRightSide) (startAngle - (index * angleStep)) else (startAngle + (index * angleStep)))
            val targetX = centerX + radius * cos(angle)
            val targetY = centerY + radius * sin(angle)

            val binding = LayoutQuickActionItemBinding.inflate(LayoutInflater.from(context), overlayView, false)
            binding.actionButton.setImageResource(action.icon)
            binding.actionLabel.text = action.label
            
            val actionView = binding.root
            
            // CRITICAL: Measure the view so we can find its TRUE center
            actionView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val viewWidth = actionView.measuredWidth
            val viewHeight = actionView.measuredHeight

            actionView.elevation = context.resources.getDimension(com.intuit.sdp.R.dimen._20sdp) 
            actionView.x = centerX.toFloat() - viewWidth / 2
            actionView.y = centerY.toFloat() - viewHeight / 2
            actionView.scaleX = 0f
            actionView.scaleY = 0f
            actionView.alpha = 0f

            // Set listener on the FAB directly to prevent click swallowing
            binding.actionButton.setOnClickListener {
                if (ClickHandler.isClickAllowed()) {
                    dismiss()
                    action.onClick()
                }
            }

            // Also set on the label/container for a larger touch target
            actionView.setOnClickListener {
                if (ClickHandler.isClickAllowed()) {
                    dismiss()
                    action.onClick()
                }
            }

            overlayView?.addView(actionView)
            buttons.add(actionView)

            // Animate each button to its center-aligned position
            val animX = ObjectAnimator.ofFloat(actionView, "x", targetX.toFloat() - viewWidth / 2)
            val animY = ObjectAnimator.ofFloat(actionView, "y", targetY.toFloat() - viewHeight / 2)
            val animScaleX = ObjectAnimator.ofFloat(actionView, "scaleX", 1f)
            val animScaleY = ObjectAnimator.ofFloat(actionView, "scaleY", 1f)
            val animAlpha = ObjectAnimator.ofFloat(actionView, "alpha", 1f)

            AnimatorSet().apply {
                playTogether(animX, animY, animScaleX, animScaleY, animAlpha)
                duration = 300
                interpolator = OvershootInterpolator(1.2f)
                startDelay = (index * 50).toLong()
                start()
            }
        }
    }

    private fun animateEntry() {
        ghostImageView?.let {
            // Fix sharp borders: Create a rounded corner outline
            it.outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    val cornerRadius = context.resources.getDimension(com.intuit.sdp.R.dimen._4sdp)
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }
            it.clipToOutline = true
            it.elevation = context.resources.getDimension(com.intuit.sdp.R.dimen._4sdp) // Low layer
            it.setLayerType(View.LAYER_TYPE_HARDWARE, null) // Anti-aliasing

            val scaleX = ObjectAnimator.ofFloat(it, "scaleX", 1.1f)
            val scaleY = ObjectAnimator.ofFloat(it, "scaleY", 1.1f)
            val rotation = ObjectAnimator.ofFloat(it, "rotation", 5f)
            
            AnimatorSet().apply {
                playTogether(scaleX, scaleY, rotation)
                duration = 300
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }

    fun isShowing(): Boolean = overlayView != null

    fun dismiss() {
        if (isDismissing || overlayView == null) return
        isDismissing = true

        val animSet = AnimatorSet()
        val anims = mutableListOf<Animator>()

        // 1. Animate Poster back to original state
        ghostImageView?.let {
            anims.add(ObjectAnimator.ofFloat(it, "scaleX", 1.0f))
            anims.add(ObjectAnimator.ofFloat(it, "scaleY", 1.0f))
            anims.add(ObjectAnimator.ofFloat(it, "rotation", 0f))
        }

        // 2. Animate Buttons pop in
        buttons.forEach { button ->
            anims.add(ObjectAnimator.ofFloat(button, "scaleX", 0f))
            anims.add(ObjectAnimator.ofFloat(button, "scaleY", 0f))
            anims.add(ObjectAnimator.ofFloat(button, "alpha", 0f))
        }

        // 3. Fade out background
        overlayView?.let {
            anims.add(ObjectAnimator.ofFloat(it, "alpha", 0f))
        }

        animSet.playTogether(anims)
        animSet.duration = 250
        animSet.interpolator = AnticipateInterpolator()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                cleanup()
            }
        })
        animSet.start()
    }

    private fun cleanup() {
        val parent = overlayView?.parent as? ViewGroup
        parent?.removeView(overlayView)
        overlayView = null
        buttons.clear()
        ghostImageView = null
        isDismissing = false
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private data class ActionInfo(val icon: Int, val label: String, val onClick: () -> Unit)
}
