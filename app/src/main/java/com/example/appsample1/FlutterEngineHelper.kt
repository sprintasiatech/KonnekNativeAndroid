package com.example.appsample1

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.appsample1.support.AppLoggerCS
import com.example.appsample1.support.DataGetConfig
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
import org.json.JSONObject

object FlutterEngineHelper {
    private var flutterEngine: FlutterEngine? = null
    private const val ENGINE_ID = "sag_main_engine"
    private const val CHANNEL_ID = "konnek_native"
    private lateinit var channel: MethodChannel

    private fun registerLifecycle(context: Context) {
        val application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                // Start engine here if appropriate
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityStarted] Activity started: ${activity.localClassName}"
//                )
            }

            override fun onActivityStopped(activity: Activity) {
//                disposeEngine()
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityStopped] Activity stopped: ${activity.localClassName}"
//                )
            }

            override fun onActivityDestroyed(activity: Activity) {
                disposeEngine()
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityDestroyed] Activity destroyed: ${activity.localClassName}"
//                )
            }

            // Required empty implementations
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityCreated] Activity created: ${activity.localClassName}"
//                )
            }

            override fun onActivityResumed(activity: Activity) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityResumed] Activity resumed: ${activity.localClassName}"
//                )
            }

            override fun onActivityPaused(activity: Activity) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityPaused] Activity paused: ${activity.localClassName}"
//                )
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivitySaveInstanceState] Activity onActivitySaveInstanceState: ${activity.localClassName}"
//                )
            }
        })
    }

    fun disposeEngine() {
        if (flutterEngine != null || FlutterEngineCache.getInstance().contains(ENGINE_ID)) {
            FlutterEngineCache.getInstance().clear();
        }
    }

    fun ensureEngine(context: Context) {
        try {
            if (flutterEngine == null) {
                flutterEngine = FlutterEngine(context.applicationContext).apply {
                    navigationChannel.setInitialRoute("/")
                    dartExecutor.executeDartEntrypoint(
                        DartExecutor.DartEntrypoint.createDefault()
                    )

                    FlutterEngineCache.getInstance().put(ENGINE_ID, this)
                }
                registerLifecycle(context)
                callConfigViaNative(context)
            }
        } catch (e: Exception) {
            AppLoggerCS.debugLog("[FlutterEngineHelper][ensureEngine] exception: ${e.toString()}")
        }
    }

    var initConfigData: String = ""

    fun callConfigViaNative(context: Context) {
        try {
            if (!isNetworkAvailable(context)) {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                return
            }
            if (flutterEngine != null) {
                KonnekService().getConfig(
                    KonnekNative.clientId,
                    onSuccess = { value: String ->
                        initConfigData = value
                        val output: Map<*, *> = jsonStringToMap(value)
                        KonnekNative.triggerFloatingUIChanges?.invoke(output["data"] as Map<*, *>)
                    },
                    onFailed = { errorMessage: String ->
                        // println("[FlutterEngineHelper][callConfigViaNative][onFailed] errorMessage: $errorMessage")
                    },
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun jsonStringToMap(jsonString: String): Map<*, *> {
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            when (value) {
                is JSONObject -> map[key] = jsonStringToMap(value.toString())
                // is JSONArray -> map[key] = jsonArrayToList(value)
                else -> map[key] = value
            }
        }
        return map.toMap()
    }

    private fun callConfig(engineInput: FlutterEngine) {
        AppLoggerCS.debugLog("[FlutterEngineHelper][callConfig]")
        // val engine = FlutterEngineCache.getInstance().get(ENGINE_ID)
        val engine = engineInput
        if (engine != null) {
            channel = MethodChannel(
                engine.dartExecutor.binaryMessenger,
                CHANNEL_ID,
            )
            val arguments = hashMapOf<String, String>()
            arguments["clientId"] = KonnekNative.clientId
            arguments["clientSecret"] = KonnekNative.clientSecret
            arguments["flavor"] = KonnekNative.flavor
            val sendData: String = Gson().toJson(arguments)
            channel.invokeMethod("clientConfigChannel", sendData)
            if (initConfigData != "") {
                channel.invokeMethod("fetchConfigData", initConfigData)
            }

            channel.setMethodCallHandler { call, result ->
                if (call.method == "configData") {
                    val map: Map<*, *> = call.arguments as Map<*, *>
                    result.success("success")
                } else if (call.method == "disposeEngine") {
                    result.success("success dispose engine")
                } else {
                    result.notImplemented()
                }
            }
        }
    }

    fun launchFlutter(context: Context) {
        try {
            val engine = FlutterEngineCache.getInstance().get(ENGINE_ID)
            if (engine != null) {
                callConfig(engine)

                val intent = FlutterActivity
                    .withCachedEngine(ENGINE_ID)
                    .backgroundMode(FlutterActivityLaunchConfigs.BackgroundMode.transparent)
                    .build(context)
                context.startActivity(intent)
            } else {
                FlutterEngineCache.getInstance().put(ENGINE_ID, flutterEngine)
                launchFlutter(context)
            }
        } catch (e: Exception) {
            AppLoggerCS.debugLog("[FlutterEngineHelper][launchFlutter] exception: ${e.toString()}")
        }
    }

    fun envInit(flavor: String) {
        when (flavor) {
            "development" -> {
                EnvironmentConfig.flavor = Flavor.DEVELOPMENT
            }

            "staging" -> {
                EnvironmentConfig.flavor = Flavor.STAGING
            }

            "production" -> {
                EnvironmentConfig.flavor = Flavor.PRODUCTION
            }

            "canary" -> {
                EnvironmentConfig.flavor = Flavor.CANARY
            }

            else -> {
                EnvironmentConfig.flavor = Flavor.STAGING
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
