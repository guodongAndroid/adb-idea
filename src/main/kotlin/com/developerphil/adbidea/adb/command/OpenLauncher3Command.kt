package com.developerphil.adbidea.adb.command

import com.android.ddmlib.IDevice
import com.android.ddmlib.MultiLineReceiver
import com.developerphil.adbidea.adb.AdbUtil
import com.developerphil.adbidea.ui.NotificationHelper
import com.google.common.base.Joiner
import com.google.common.base.Strings
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet
import java.util.*
import java.util.concurrent.TimeUnit

class OpenLauncher3Command : Command {

    companion object {
        private const val LAUNCHER3_PACKAGE_NAME = "com.android.launcher3"
        private const val LAUNCHER3_ACTIVITY_NAME = "com.android.launcher3.Launcher"
        private const val LAUNCHER3_ACTIVITY_NAME_R = "com.android.launcher3.uioverrides.QuickstepLauncher"
    }

    override fun run(project: Project, device: IDevice, facet: AndroidFacet, packageName: String): Boolean {
        try {
            if (AdbUtil.isAppInstalled(device, LAUNCHER3_PACKAGE_NAME)) {
                val receiver = StartActivityReceiver()
                val apiLevel = device.version.apiLevel
                val activityName = if (apiLevel == 30) {
                    // A30/A35 Android R API 30
                    LAUNCHER3_ACTIVITY_NAME_R
                } else {
                    LAUNCHER3_ACTIVITY_NAME
                }
                device.executeShellCommand("am start -n $LAUNCHER3_PACKAGE_NAME/$activityName", receiver, 15L, TimeUnit.SECONDS)
                if (receiver.isSuccess) {
                    NotificationHelper.info(String.format("<b>%s</b> started on %s", LAUNCHER3_PACKAGE_NAME, device.name))
                    return true
                } else {
                    NotificationHelper.error(String.format("<b>%s</b> could not be started on %s. \n\n<b>ADB Output:</b> \n%s", LAUNCHER3_PACKAGE_NAME, device.name, receiver.message))
                }
            } else {
                NotificationHelper.error(String.format("<b>%s</b> is not installed on %s", LAUNCHER3_PACKAGE_NAME, device.name))
            }
        } catch (e1: Exception) {
            NotificationHelper.error("Start app failed... " + e1.message)
        }
        return false
    }

    class StartActivityReceiver : MultiLineReceiver() {
        var message = "Nothing Received"
        var currentLines: MutableList<String?> = ArrayList()
        override fun processNewLines(strings: Array<String>) {
            for (s in strings) {
                if (!Strings.isNullOrEmpty(s)) {
                    currentLines.add(s)
                }
            }
            computeMessage()
        }

        private fun computeMessage() {
            message = Joiner.on("\n").join(currentLines)
        }

        override fun isCancelled(): Boolean {
            return false
        }

        val isSuccess: Boolean
            get() = currentLines.size in 1..2
    }
}