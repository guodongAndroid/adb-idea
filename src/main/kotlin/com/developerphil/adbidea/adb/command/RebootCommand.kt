package com.developerphil.adbidea.adb.command

import com.android.ddmlib.IDevice
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver
import com.developerphil.adbidea.ui.NotificationHelper
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet
import java.util.concurrent.TimeUnit

class RebootCommand : Command {
    override fun run(project: Project, device: IDevice, facet: AndroidFacet, packageName: String): Boolean {
        try {
            device.executeShellCommand("reboot", GenericReceiver(), 15L, TimeUnit.SECONDS)
            NotificationHelper.info(String.format("reboot on %s", device.name))
            return true
        } catch (e1: Exception) {
            NotificationHelper.error("Reboot failed... " + e1.message)
        }
        return false
    }
}