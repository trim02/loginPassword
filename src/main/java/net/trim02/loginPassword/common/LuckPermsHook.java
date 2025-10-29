package net.trim02.loginPassword.common;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import java.util.UUID;

public class LuckPermsHook {

    public static LuckPerms api = LuckPermsProvider.get();

    public static void addNode(UUID uuid, String node) {
        System.out.println("Adding node " + node + " to user " + uuid);
        api.getUserManager().modifyUser(uuid, user -> {
            user.data().add(net.luckperms.api.node.Node.builder(node).build());
        });

    }

}
