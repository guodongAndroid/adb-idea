package com.developerphil.adbidea.adb.command

import com.android.ddmlib.IDevice
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver
import com.developerphil.adbidea.ui.NotificationHelper
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet
import java.util.concurrent.TimeUnit

class MVPMonitorCommand(private val cmd: Int) : Command {

    companion object {
        const val CMD_DEBUG_VIEW = 1
        const val CMD_DUMP = 2
        const val CMD_DUMP_TRACES = 3
    }

    override fun run(project: Project, device: IDevice, facet: AndroidFacet, packageName: String): Boolean {
        val shell = "am broadcast -a mvp.$packageName.monitor --ei cmd $cmd"
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