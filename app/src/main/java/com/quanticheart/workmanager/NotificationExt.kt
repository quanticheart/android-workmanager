@file:Suppress("unused")

package com.quanticheart.custonnotifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.StrictMode
import android.widget.RemoteViews
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.quanticheart.custonnotifications.ResourcesHelper.getStringResourceByKey
import java.io.IOException
import java.net.URL
import java.util.Random

fun Context.debugNotification(msg: String) {
    notify()
        .setTitle("Test")
        .setMessage(msg)
        .show()
}

fun Context.notify(): Notify {
    return Notify(this)
}

fun Context.notifyCancel(id: Int) {
    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(id)
}

fun Context.notifyCancelAll() {
    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
}

class Notify(private val context: Context) {
    enum class NotifyImportance {
        MIN, LOW, HIGH, MAX
    }

    interface ChannelData {
        companion object {
            const val ID = "notify_channel_id"
            const val NAME = "notify_channel_name"
            const val DESCRIPTION = "notify_channel_description"
        }
    }

    private val notificationBuilder: NotificationCompat.Builder
    private var customLayout: RemoteViews? = null
    private var customBigLayout: RemoteViews? = null
    private var channelId: String? = null
    private var channelName: String? = null
    private var channelDescription: String? = null
    private var title: CharSequence = ""
    private var message: CharSequence = ""
    private var id: Int
    private var smallIcon: Int
    private var oreoImportance = 0
    private var color = -1
    private var largeIcon: Any
    private var picture: Any? = null
    private var action: Intent? = null
    private var vibrationPattern = longArrayOf(0, 250, 250, 250)
    private var autoCancel = false
    private var vibration = true
    private var circle = false

    init {
        val applicationInfo = context.applicationInfo

        /*
        * Default values:
        * */
        id = notificationID()
        largeIcon = applicationInfo.icon
        smallIcon = applicationInfo.icon

        setDefaultPriority()
        channelId = try {
            getStringResourceByKey(context, ChannelData.ID)
        } catch (e: Resources.NotFoundException) {
            "NotifyAndroid"
        }
        channelName = try {
            getStringResourceByKey(context, ChannelData.NAME)
        } catch (e: Resources.NotFoundException) {
            "NotifyAndroidChannel"
        }
        channelDescription = try {
            getStringResourceByKey(context, ChannelData.DESCRIPTION)
        } catch (e: Resources.NotFoundException) {
            "Default notification android channel"
        }
        notificationBuilder = NotificationCompat.Builder(context, channelId!!)
    }

    fun cancel(id: Int) {
        val notificationManager =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.cancel(id)
    }

    fun cancelAll() {
        val notificationManager =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.cancelAll()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun show() {
        val notificationManager =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        notificationBuilder.setAutoCancel(autoCancel)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(smallIcon)

        if (customLayout != null) {
            notificationBuilder
                .setCustomContentView(customLayout)
            if (customBigLayout != null) {
                notificationBuilder.setCustomBigContentView(customBigLayout)
            } else {
                notificationBuilder.setCustomBigContentView(customLayout)
            }
        } else {
            notificationBuilder.setAutoCancel(autoCancel)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }

        /*
         * Set large icon
         * */
        var largeIconBitmap: Bitmap?
        largeIconBitmap =
            if (largeIcon is String) BitmapHelper.getBitmapFromUrl(largeIcon.toString()) else BitmapHelper.getBitmapFromRes(
                context, largeIcon as Int
            )

        /*
         * Circular large icon for chat messages
         * */if (largeIconBitmap != null) {
            if (circle) largeIconBitmap = BitmapHelper.toCircleBitmap(largeIconBitmap)
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }

        /*
         * Set notification color
         * */if (picture != null) {
            val pictureBitmap: Bitmap? =
                if (picture is String) BitmapHelper.getBitmapFromUrl(picture.toString()) else BitmapHelper.getBitmapFromRes(
                    context, picture as Int
                )
            if (pictureBitmap != null) {
                val bigPictureStyle = NotificationCompat.BigPictureStyle().bigPicture(pictureBitmap)
                    .setSummaryText(message)
                bigPictureStyle.bigLargeIcon(largeIconBitmap)
                notificationBuilder.setStyle(bigPictureStyle)
            }
        }

        /*
        * Set notification color
        * */
        val realColor: Int = if (color == -1) Color.BLACK else context.resources.getColor(
            color,
            null
        )
        notificationBuilder.color = realColor

        /*
        * Oreo^ notification channels
        * */if (SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, oreoImportance
            )
            notificationChannel.description = channelDescription
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = realColor
            notificationChannel.enableVibration(vibration)
            notificationChannel.vibrationPattern = vibrationPattern
            notificationManager.createNotificationChannel(notificationChannel)
        }

        /*
        * Set vibration pattern
        * */if (vibration) notificationBuilder.setVibrate(vibrationPattern) else notificationBuilder.setVibrate(
            longArrayOf(0)
        )

        /*
        * Action triggered when user clicks noti
        * */if (action != null) {
            val pi: PendingIntent = PendingIntent.getActivity(
                context, id,
                action,
                FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pi)
        }

        /*
        * Show built notification
        * */notificationManager.notify(id, notificationBuilder.build())
    }

    fun setTitle(title: CharSequence): Notify {
        this.title = title
        return this
    }

    fun setMessage(message: CharSequence): Notify {
        this.message = message
        return this
    }

    fun setChannelId(channelId: String): Notify {
        if (channelId.isNotEmpty()) {
            this.channelId = channelId
            notificationBuilder.setChannelId(channelId)
        }
        return this
    }

    fun setChannelName(channelName: String): Notify {
        if (channelName.isNotEmpty()) this.channelName = channelName
        return this
    }

    fun setChannelDescription(channelDescription: String): Notify {
        if (channelDescription.isNotEmpty()) this.channelDescription = channelDescription
        return this
    }

    fun setImportance(importance: NotifyImportance): Notify {
        oreoImportance = when (importance) {
            NotifyImportance.MIN -> {
                NotificationManager.IMPORTANCE_MIN
            }

            NotifyImportance.LOW -> {
                NotificationManager.IMPORTANCE_LOW
            }

            NotifyImportance.HIGH -> {
                NotificationManager.IMPORTANCE_HIGH
            }

            NotifyImportance.MAX -> {
                NotificationManager.IMPORTANCE_MAX
            }
        }
        return this
    }

    private fun setDefaultPriority() {
        oreoImportance = NotificationManager.IMPORTANCE_DEFAULT
    }

    fun enableVibration(vibration: Boolean): Notify {
        this.vibration = vibration
        return this
    }

    fun setAutoCancel(autoCancel: Boolean): Notify {
        this.autoCancel = autoCancel
        return this
    }

    fun largeCircularIcon(): Notify {
        circle = true
        return this
    }

    fun setVibrationPattern(vibrationPattern: LongArray): Notify {
        this.vibrationPattern = vibrationPattern
        return this
    }

    fun setColor(@ColorRes color: Int): Notify {
        this.color = color
        return this
    }

    fun setSmallIcon(@DrawableRes smallIcon: Int): Notify {
        this.smallIcon = smallIcon
        return this
    }

    fun setLargeIcon(@DrawableRes largeIcon: Int): Notify {
        this.largeIcon = largeIcon
        return this
    }

    fun setLargeIcon(largeIconUrl: String): Notify {
        largeIcon = largeIconUrl
        return this
    }

    fun setPicture(@DrawableRes pictureRes: Int): Notify {
        picture = pictureRes
        return this
    }

    fun setPicture(pictureUrl: String): Notify {
        picture = pictureUrl
        return this
    }

    fun setAction(action: Intent): Notify {
        this.action = action
        return this
    }

    fun setId(id: Int): Notify {
        this.id = id
        return this
    }

    fun setCustomLayout(resourceLayout: Int, callback: (RemoteViews) -> Unit): Notify {
        this.customLayout = RemoteViews(context.packageName, resourceLayout)
        callback(this.customLayout!!)
        return this
    }

    fun setCustomBigLayout(resourceLayout: Int, callback: (RemoteViews) -> Unit): Notify {
        this.customBigLayout = RemoteViews(context.packageName, resourceLayout)
        callback(this.customBigLayout!!)
        return this
    }
}

object ResourcesHelper {
    fun getStringResourceByKey(context: Context, resourceKey: String): String {
        val resId = context.resources.getIdentifier(resourceKey, "string", context.packageName)
        return context.resources.getString(resId)
    }
}

object BitmapHelper {
    fun getBitmapFromUrl(url: String?): Bitmap? {
        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)
        try {
            return BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getBitmapFromRes(context: Context, res: Int?): Bitmap? {
        return res?.let { BitmapFactory.decodeResource(context.resources, it) }
    }

    fun toCircleBitmap(bitmap: Bitmap): Bitmap {
        val dstBmp: Bitmap = if (bitmap.width > bitmap.height) {
            Bitmap.createBitmap(
                bitmap,
                (bitmap.width - bitmap.height) / 2,  //bitmap.getWidth()/2 - bitmap.getHeight()/2,
                0,
                bitmap.height,
                bitmap.height
            )
        } else {
            Bitmap.createBitmap(
                bitmap,
                0,
                (bitmap.height - bitmap.width) / 2,
                bitmap.width,
                bitmap.width
            )
        }
        val output = Bitmap.createBitmap(
            dstBmp.width,
            dstBmp.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color: Int = Color.BLACK
        val paint = Paint()
        val rect = Rect(0, 0, dstBmp.width, dstBmp.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(dstBmp, rect, rect, paint)
        return output
    }
}

private fun notificationID(): Int {
    val rand = Random()
    return rand.nextInt(1000) + 500
}
