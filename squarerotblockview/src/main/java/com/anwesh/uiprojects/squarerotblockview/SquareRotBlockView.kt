package com.anwesh.uiprojects.squarerotblockview

/**
 * Created by anweshmishra on 08/10/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val parts : Int = 2
val scGap : Float = 0.02f
val delay : Long = 20
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val squareSizeFactor : Float = 3f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawSquareLine(i : Int, size : Float, scale : Float, paint : Paint) {
    val sqSize : Float = (size / squareSizeFactor) * (i * scale.divideScale(0, parts) + (1 - i))
    save()
    rotate(-90f * scale.divideScale(0, parts) * i)
    drawLine(0f, 0f, size, 0f, paint)
    save()
    translate(size * scale.divideScale(1, parts), 0f)
    drawRect(RectF(-sqSize, -sqSize, sqSize, sqSize), paint)
    restore()
    restore()
}

fun Canvas.drawSquareLines(size : Float, scale : Float, paint : Paint) {
    for (j in 0..(parts - 1)) {
        drawSquareLine(j, size, scale, paint)
    }
}

fun Canvas.drawSRLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawSquareLines(size, scale, paint)
    restore()
}

class SquareRotBlockView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SRLNode(var i : Int, val state : State = State()) {

        private var next : SRLNode? = null
        private var prev : SRLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SRLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSRLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SRLNode {
            var curr : SRLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SquareRotBlock(var i : Int) {

        private val root : SRLNode = SRLNode(0)
        private var curr : SRLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SquareRotBlockView) {

        private val animator : Animator = Animator(view)
        private val srb : SquareRotBlock = SquareRotBlock(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            srb.draw(canvas, paint)
            animator.animate {
                srb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            srb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : SquareRotBlockView {
            val view : SquareRotBlockView = SquareRotBlockView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
