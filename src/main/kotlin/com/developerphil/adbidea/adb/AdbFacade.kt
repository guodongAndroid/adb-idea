package com.developerphil.adbidea.adb

import com.developerphil.adbidea.ObjectGraph
import com.developerphil.adbidea.adb.DeviceResult.SuccessfulDeviceResult
import com.developerphil.adbidea.adb.command.*
import com.developerphil.adbidea.adb.command.SvcCommand.MOBILE
import com.developerphil.adbidea.adb.command.SvcCommand.WIFI
import com.developerphil.adbidea.ui.NotificationHelper
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.intellij.openapi.project.Project
import java.util.concurrent.Executors

object AdbFacade {
    private val EXECUTOR = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("AdbIdea-%d").build())

    fun uninstall(project: Project) = executeOnDevice(project, UninstallCommand())
    fun kill(project: Project) = executeOnDevice(project, KillCommand())
    fun grantPermissions(project: Project) = executeOnDevice(project, GrantPermissionsCommand())
    fun revokePermissions(project: Project) = executeOnDevice(project, RevokePermissionsCommand())
    fun revokePermissionsAndRestart(project: Project) = executeOnDevice(project, RevokePermissionsAndRestartCommand())
    fun startDefaultActivity(project: Project) = executeOnDevice(project, StartDefaultActivityCommand(false))
    fun startDefaultActivityWithDebugger(project: Project) = executeOnDevice(project, StartDefaultActivityCommand(true))
    fun restartDefaultActivity(project: Project) = executeOnDevice(project, RestartPackageCommand())
    fun restartDefaultActivityWithDebugger(project: Project) =
        executeOnDevice(project, CommandList(KillCommand(), StartDefaultActivityCommand(true)))

    fun clearData(project: Project) = executeOnDevice(project, ClearDataCommand())
    fun clearDataAndRestart(project: Project) = executeOnDevice(project, ClearDataAndRestartCommand())
    fun clearDataAndRestartWithDebugger(project: Project) =
        executeOnDevice(project, ClearDataAndRestartWithDebuggerCommand())

    fun enableWifi(project: Project) = executeOnDevice(project, ToggleSvcCommand(WIFI, true))
    fun disableWifi(project: Project) = executeOnDevice(project, ToggleSvcCommand(WIFI, false))
    fun enableMobile(project: Project) = executeOnDevice(project, ToggleSvcCommand(MOBILE, true))
    fun disableMobile(project: Project) = executeOnDevice(project, ToggleSvcCommand(MOBILE, false))

    fun reboot(project: Project) = executeOnDevice(project, RebootCommand())
    fun openLauncher3(project: Project) = executeOnDevice(project, OpenLauncher3Command())
    fun openSystemSettings(project: Project) = executeOnDevice(project, OpenSystemSettingsCommand())
    fun openApplicationDevelopmentSettings(project: Project) = executeOnDevice(project, OpenApplicationDevelopmentSettingsCommand())
    fun inputKeyEventBack(project: Project) = executeOnDevice(project, InputKeyEventCommand(InputKeyEventCommand.BACK))
    fun inputKeyEventUp(project: Project) = executeOnDevice(project, InputKeyEventCommand(InputKeyEventCommand.UP))
    fun inputKeyEventDown(project: Project) = executeOnDevice(project, InputKeyEventCommand(InputKeyEventCommand.DOWN))
    fun inputKeyEventLeft(project: Project) = executeOnDevice(project, InputKeyEventCommand(InputKeyEventCommand.LEFT))
    fun inputKeyEventRight(project: Project) = executeOnDevice(project, InputKeyEventCommand(InputKeyEventCommand.RIGHT))
    fun inputKeyEventCenter(project: Project) = executeOnDevice(project, InputKeyEventCommand(InputKeyEventCommand.CENTER))

    private fun executeOnDevice(project: Project, runnable: Command) {
        if (AdbUtil.isGradleSyncInProgress(project)) {
            NotificationHelper.error("Gradle sync is in progress")
            return
        }

        val objectGraph = project.getService(ObjectGraph::class.java)
        when (val result = objectGraph.deviceResultFetcher.fetch()) {
            is SuccessfulDeviceResult -> {
                result.devices.forEach { device ->
                    EXECUTOR.submit {
                        runnable.run(
                            CommandContext(
                                project = project,
                                device = device,
                                facet = result.facet,
                                packageName = result.packageName,
                                coroutineScope = objectGraph.projectScope
                            )
                        )
                    }
                }
            }

            is DeviceResult.Cancelled -> Unit
            is DeviceResult.DeviceNotFound, null -> NotificationHelper.error("No device found")
        }
    }
}
