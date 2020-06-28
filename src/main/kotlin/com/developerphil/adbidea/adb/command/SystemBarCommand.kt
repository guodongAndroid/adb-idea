package com.developerphil.adbidea.adb.command

import com.android.ddmlib.IDevice
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver
import com.developerphil.adbidea.ui.NotificationHelper
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet
import java.util.concurrent.TimeUnit

class SystemBarCommand(show: Boolean) : Command {

    private val shell = "am broadcast -a ${if (show) "android.intent.action.SHOW_SYSTEM_BAR" else "android.intent.action.HIDE_SYSTEM_BAR"}"

    override fun run(project: Project, device: IDevice, facet: AndroidFacet, packageName: String): Boolean {
        try {
            device.executeShellCommand(shell, GenericReceiver(), 15L, TimeUnit.SECONDS)
            NotificationHelper.info(String.format("$shell on %s", device.name))
            return true
        } catch (e1: Exception) {
            NotificationHelper.error("$shell failed... " + e1.message)
        }
        return false
    }
}