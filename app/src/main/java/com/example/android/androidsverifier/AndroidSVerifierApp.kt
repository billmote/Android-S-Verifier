package com.example.android.androidsverifier

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.salesforce.marketingcloud.MCLogListener
import com.salesforce.marketingcloud.MCLogListener.Companion.VERBOSE
import com.salesforce.marketingcloud.MarketingCloudConfig
import com.salesforce.marketingcloud.MarketingCloudSdk
import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkModuleConfig
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogLevel
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogListener
import kotlin.random.Random

class AndroidSVerifierApp : Application() {
    override fun onCreate() {
        super.onCreate()
        runIfDebug {
            SFMCSdk.setLogging(LogLevel.DEBUG, LogListener.AndroidLogger())
            MarketingCloudSdk.setLogLevel(VERBOSE)
            MarketingCloudSdk.setLogListener(MCLogListener.AndroidLogListener())
        }
        SFMCSdk.configure(applicationContext as Application, SFMCSdkModuleConfig.build {
            pushModuleConfig = MarketingCloudConfig.builder().apply {
                setApplicationId(getString(R.string.PUSH_APPLICATION_ID))
                    .setAccessToken(getString(R.string.PUSH_ACCESS_TOKEN))
                    .setSenderId(getString(R.string.PUSH_SENDER_ID))
                    .setMarketingCloudServerUrl(getString(R.string.PUSH_TSE))
                setNotificationCustomizationOptions(
                    NotificationCustomizationOptions.create(
                        R.mipmap.ic_launcher,
                        { context, message ->
                            message.url?.let {
                                handleUrlPendingIntent(context, it)
                            }.orElse {
                                openAppPendingIntent(context)
                            }
                        }, // Tell the SDK how to handle notification clicks
                        null // Use SDK's default channel or replace null with your channel
                    )
                )
                setAnalyticsEnabled(true)
                setUrlHandler { context, url, _ ->
                    handleUrlPendingIntent(context, url)
                } // Handle InApp Message URLs
            }.build(applicationContext)
        }) { _ ->
            runIfDebug {
                SFMCSdk.requestSdk { sdk ->
                    Log.i("~#STATE", sdk.getSdkState().toString(2))
                    sdk.mp { push ->
                        push.registrationManager.registerForRegistrationEvents {
                            Log.i("~#REGISTRATION", it.toString())
                        }
                    }
                }
            }
        }
    }

    private fun handleUrlPendingIntent(
        context: Context,
        url: String
    ) = PendingIntent.getActivity(
        context,
        Random.nextInt(),
        Intent(Intent.ACTION_VIEW, Uri.parse(url)),
        setIntentFlags()
    )

    private fun openAppPendingIntent(context: Context) = PendingIntent.getActivity(
        context,
        java.util.Random().nextInt(),
        context.packageManager.getLaunchIntentForPackage(context.packageName),
        setIntentFlags()
    )

    private fun setIntentFlags() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
}

inline fun <R> R?.orElse(block: () -> R): R {
    return this ?: block()
}

fun runIfDebug(block: () -> Any?): Any? = if (BuildConfig.DEBUG) block() else Unit