package com.developerphil.adbidea.adb.command

import com.android.ddmlib.MultiLineReceiver
import com.developerphil.adbidea.ui.NotificationHelper
import com.google.common.base.Joiner
import com.google.common.base.Strings
import java.util.concurrent.TimeUnit

class OpenApplicationDevelopmentSettingsCommand : Command {

    companion object {
        private const val APPLICATION_DEVELOPMENT_SETTINGS = "android.settings.APPLICATION_DEVELOPMENT_SETTINGS"
    }

    override fun run(context: CommandContext): Boolean = with(context) {
        try {
            val receiver = StartActivityReceiver()
            device.executeShellCommand(
                "am start -a android.settings.APPLICATION_DEVELOPMENT_SETTINGS",
                receiver,
                15L,
                TimeUnit.SECONDS
            )
            if (receiver.isSuccess) {
                NotificationHelper.info(
                    String.format(
                        "<b>%s</b> started on %s",
                        APPLICATION_DEVELOPMENT_SETTINGS,
                        device.name
                    )
                )
                return true
            } else {
                NotificationHelper.error(
                    String.format(
                        "<b>%s</b> could not be started on %s. \n\n<b>ADB Output:</b> \n%s",
                        APPLICATION_DEVELOPMENT_SETTINGS,
                        device.name,
                        receiver.message
                    )
                )
            }
        } catch (e1: Exception) {
            NotificationHelper.error("Open application development settings failed... " + e1.message)
        }
        return false
    }

    class StartActivityReceiver : MultiLineReceiver() {
        var message = "Nothing Received"
        private val currentLines: MutableList<String?> = ArrayList()
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