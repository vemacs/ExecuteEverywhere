package me.vemacs.executeeverywhere.common;

public enum EEPlatform {
    BUKKIT {
        @Override
        public boolean current() {
            try {
                Class.forName("org.bukkit.Bukkit");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    },
    BUNGEECORD {
        @Override
        public boolean current() {
            try {
                Class.forName("net.md_5.bungee.api.ProxyServer");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    };

    public abstract boolean current();
}
