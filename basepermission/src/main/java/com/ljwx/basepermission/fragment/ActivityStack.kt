package com.ljwx.basepermission.fragment

import android.app.Activity
import android.os.Build
import androidx.annotation.NonNull
import java.util.Stack


class ActivityStack {

    companion object {
        private val sInstance: ActivityStack = ActivityStack()

        /**
         * 单例
         */
        fun getInstance(): ActivityStack {
            return sInstance
        }
    }

    private val lock = Any()
    private val stack = Stack<Activity?>()

    private constructor()


    /**
     * 压入堆栈顶部
     */
    fun <A : Activity?> push(@NonNull activity: A) {
        synchronized(lock) { stack.push(activity) }
    }

    /**
     * 获取当前Activity(最后一个入栈的)
     */
    fun curr(): Activity? {
        synchronized(lock) {
            return try {
                stack.lastElement()
            } catch (ex: Exception) {
                null
            }
        }
    }

    /**
     * 移除堆栈中的Activity，并将Activity Finish
     */
    fun pop(activity: Activity?) {
        synchronized(lock) { pop(activity, true) }
    }

    /**
     * 移除堆栈中的Activity
     */
    fun pop(activity: Activity?, finish: Boolean) {
        synchronized(lock) {
            if (activity != null && !activity.isFinishing && !activity.isDestroyed && finish) {
                activity.finish()
            }
            remove(activity)
        }
    }

    /**
     * 移除栈中的所有Activity
     */
    fun popAll() {
        synchronized(lock) {
            if (stack.isEmpty()) {
                return
            }
            try {
                val it = stack.iterator()
                while (it.hasNext()) {
                    val activity = it.next()
                    if (activity != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            if (!activity.isFinishing && !activity.isDestroyed) {
                                activity.finish()
                            }
                        } else {
                            if (!activity.isFinishing) {
                                activity.finish()
                            }
                        }
                    }
                    it.remove()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 移除栈
     */
    fun remove(activity: Activity?) {
        synchronized(lock) {
            if (stack.empty() || !stack.contains(activity)) {
                return
            }
            try {
                stack.remove(activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 返回指定位置的Activity
     */
    operator fun get(index: Int): Activity? {
        synchronized(lock) {
            if (index < 0) {
                return null
            }
            return if (!stack.empty() && index < stack.size) {
                stack[index]
            } else null
        }
    }


    /**
     * 返回前一个 Activity
     */
    fun pre(): Activity? {
        synchronized(lock) {
            val index = indexOf()
            return get(index - 1)
        }
    }

    /**
     * 当前Activity索引位置
     */
    fun indexOf(): Int {
        synchronized(lock) {
            val activity = curr() ?: return -1
            return if (!stack.empty() && stack.contains(activity)) {
                stack.indexOf(activity)
            } else -1
        }
    }

    fun <A : Activity?> indexOf(@NonNull activity: A): Int {
        synchronized(lock) {
            return if (!stack.empty() && stack.contains(activity)) {
                stack.indexOf(activity)
            } else -1
        }
    }

}