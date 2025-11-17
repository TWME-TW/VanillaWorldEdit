package dev.twme.vanillaworldedit.task;

import dev.twme.vanillaworldedit.proxy.PluginMessageUtil;

public class FinishCommandNoticeTask implements Runnable{
    @Override
    public void run() {
        if (PluginMessageUtil.lastCommandTime.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        PluginMessageUtil.lastCommandTime.forEach((player, time) -> {

            if (!player.isOnline()) {
                PluginMessageUtil.commandCount.remove(player);
                PluginMessageUtil.lastCommandTime.remove(player);
                PluginMessageUtil.firstCommandTime.remove(player);
                return;
            }
            if (currentTime - time > 5000) {
                player.sendMessage("Finished " + PluginMessageUtil.commandCount.get(player) + " place commands in " + (currentTime - PluginMessageUtil.firstCommandTime.get(player))/1000 + "s");
                PluginMessageUtil.commandCount.remove(player);
                PluginMessageUtil.lastCommandTime.remove(player);
                PluginMessageUtil.firstCommandTime.remove(player);
            }
        });
    }
}
