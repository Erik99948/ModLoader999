package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class ModSecurityManager extends SecurityManager {
    private final JavaPlugin plugin;
    private Policy modPolicy;
    private final Map<String, ProtectionDomain> modProtectionDomains = new HashMap<>();

    public ModSecurityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            // Set the system-wide policy to our custom policy
            System.setProperty("java.security.policy", new File(plugin.getDataFolder(), "mod.policy").toURI().toURL().toString());
            modPolicy = Policy.getInstance("JavaPolicy", null);
            modPolicy.refresh();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load mod.policy: " + e.getMessage());
            modPolicy = Policy.getPolicy(); // Fallback to default policy
        }
    }

    public void addModProtectionDomain(String modName, ProtectionDomain domain) {
        modProtectionDomains.put(modName, domain);
    }

    @Override
    public void checkPermission(Permission perm) {
        // Allow system classes to do anything
        if (isSystemClass(getClassContext())) {
            return;
        }

        ProtectionDomain currentDomain = getCurrentProtectionDomain();
        if (currentDomain != null && modProtectionDomains.containsValue(currentDomain)) {
            // This is mod code, check against our custom policy
            if (!modPolicy.implies(currentDomain, perm)) {
                plugin.getLogger().warning("Mod code denied permission: " + perm.getName() + " in domain " + currentDomain.getCodeSource().getLocation());
                throw new SecurityException("Permission denied by ModLoader999 policy: " + perm.getName());
            }
        } else {
            // Not mod code, or no specific domain found, use default behavior (or superclass behavior)
            super.checkPermission(perm);
        }
    }

    private boolean isSystemClass(Class<?>[] context) {
        if (context == null || context.length == 0) {
            return true; // Should not happen, but assume system if no context
        }
        // Iterate through the call stack to find the first non-system class
        for (Class<?> clazz : context) {
            if (!clazz.getName().startsWith("java.") &&
                !clazz.getName().startsWith("javax.") &&
                !clazz.getName().startsWith("sun.") &&
                !clazz.getName().startsWith("com.sun.") &&
                !clazz.getName().startsWith("org.bukkit.") &&
                !clazz.getName().startsWith("com.example.modloader.")) {
                return false; // Found a non-system class
            }
        }
        return true; // All classes in context are system classes
    }

    private ProtectionDomain getCurrentProtectionDomain() {
        Class<?>[] context = getClassContext();
        if (context != null) {
            for (Class<?> clazz : context) {
                ProtectionDomain domain = clazz.getProtectionDomain();
                if (domain != null && domain.getCodeSource() != null) {
                    // Check if this domain is one of our registered mod domains
                    for (ProtectionDomain modDomain : modProtectionDomains.values()) {
                        if (modDomain.equals(domain)) {
                            return domain;
                        }
                    }
                }
            }
        }
        return null;
    }
}
