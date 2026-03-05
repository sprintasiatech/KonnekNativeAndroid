package com.example.appsample1

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import com.example.appsample1.support.AppLoggerCS
import com.example.appsample1.support.EnvironmentConfig
import com.example.appsample1.support.Flavor
import com.example.appsample1.support.KonnekService
import com.google.gson.Gson
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterActivityLaunchConfigs
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

object FlutterEngineHelper {

    private var flutterEngine: FlutterEngine? = null
    private const val ENGINE_ID = "sag_main_engine"
    private const val CHANNEL_ID = "konnek_native"
    private const val TAG = "[FlutterEngineHelper]"

    private var channel: MethodChannel? = null
    private var initConfigData: String = ""
    private var lifecycleRegistered = false
    private var isEnvInitialized = false

    fun envInit(flavor: String) {
        EnvironmentConfig.flavor = when (flavor) {
            "development" -> Flavor.DEVELOPMENT
            "staging" -> Flavor.STAGING
            "production" -> Flavor.PRODUCTION
            else -> {
                AppLoggerCS.debugLog("$TAG[envInit] Unknown flavor '$flavor', defaulting to STAGING")
                Flavor.STAGING
            }
        }
        isEnvInitialized = true
    }

    @Synchronized
    fun ensureEngine(context: Context) {
        check(isEnvInitialized) {
            "envInit() must be called before ensureEngine()"
        }
        if (flutterEngine != null) return

        try {
            flutterEngine = FlutterEngine(context.applicationContext).apply {
                navigationChannel.setInitialRoute("/")
                dartExecutor.executeDartEntrypoint(
                    DartExecutor.DartEntrypoint.createDefault()
                )
                FlutterEngineCache.getInstance().put(ENGINE_ID, this)
            }

            if (!lifecycleRegistered) {
                registerLifecycleCallbacks(context)
                lifecycleRegistered = true
            }

            callConfigViaNative(context)
        } catch (e: Exception) {
            AppLoggerCS.debugLog("$TAG[ensureEngine] Failed to create engine: ${e.message}")
        }
    }

    fun launchFlutter(context: Context) {
        try {
            val engine = FlutterEngineCache.getInstance().get(ENGINE_ID)
            if (engine == null) {
                AppLoggerCS.debugLog("$TAG[launchFlutter] Engine not ready — call ensureEngine() first.")
                return
            }

            setupMethodChannel(engine)

            val intent = FlutterActivity
                .withCachedEngine(ENGINE_ID)
                .backgroundMode(FlutterActivityLaunchConfigs.BackgroundMode.transparent)
                .build(context)
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLoggerCS.debugLog("$TAG[launchFlutter] Exception: ${e.message}")
        }
    }

    fun callConfigViaNative(context: Context) {
        if (!isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            AppLoggerCS.debugLog("$TAG[callConfigViaNative] Skipped — no network.")
            return
        }

        if (flutterEngine == null) {
            AppLoggerCS.debugLog("$TAG[callConfigViaNative] Skipped — engine not initialised.")
            return
        }

        KonnekService().getConfig(
            clientIdValue = KonnekNative.clientId,
            onSuccess = { value: String ->
                initConfigData = value
                val output: Map<*, *> = jsonStringToMap(value)
                KonnekNative.triggerFloatingUIChanges?.invoke(output["data"] as Map<*, *>)
            },
            onFailed = { errorMessage: String ->
                AppLoggerCS.debugLog("$TAG[callConfigViaNative] Config fetch failed: $errorMessage")
            },
        )
    }

    fun disposeEngine() {
        FlutterEngineCache.getInstance().remove(ENGINE_ID)
        flutterEngine?.destroy()
        flutterEngine = null
        channel = null
        AppLoggerCS.debugLog("$TAG[disposeEngine] Engine disposed.")
    }

    private fun setupMethodChannel(engine: FlutterEngine) {
        channel = MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL_ID).also { ch ->
            val arguments = hashMapOf(
                "clientId" to KonnekNative.clientId,
                "clientSecret" to KonnekNative.clientSecret,
                "flavor" to KonnekNative.flavor,
            )
            ch.invokeMethod("clientConfigChannel", Gson().toJson(arguments))

            if (initConfigData.isNotEmpty()) {
                ch.invokeMethod("fetchConfigData", initConfigData)
            }

            ch.setMethodCallHandler { call, result ->
                when (call.method) {
                    "configData" -> result.success("success")
                    "disposeEngine" -> {
                        result.success("success dispose engine")
                    }

                    else -> result.notImplemented()
                }
            }
        }
    }

    private fun registerLifecycleCallbacks(context: Context) {
        val application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {

            override fun onActivityDestroyed(activity: Activity) {
                AppLoggerCS.debugLog(
                    "$TAG[lifecycle] Activity destroyed: ${activity.localClassName}"
                )
                if (activity !is FlutterActivity) {
                    disposeEngine()
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }

    private fun jsonStringToMap(jsonString: String): Map<*, *> {
        return try {
            Gson().fromJson(jsonString, Map::class.java) ?: emptyMap<String, Any>()
        } catch (e: Exception) {
            AppLoggerCS.debugLog("$TAG[jsonStringToMap] Parse error: ${e.message}")
            emptyMap<String, Any>()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}