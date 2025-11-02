package net.trim02.loginPassword.common;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class LuckPermsHook {

    public static LuckPerms api = LuckPermsProvider.get();

    public static void addNode(UUID uuid, String node) {
        api.getUserManager().modifyUser(uuid, user -> {
            user.data().add(net.luckperms.api.node.Node.builder(node).build());
        });

    }
    public static User loadUser(UUID uuid) {
        return api.getUserManager().loadUser(uuid).join();
    }

}
