# ProGuard configuration for Emblems plugin

-printmapping mapping.txt

-overloadaggressively
-allowaccessmodification
-repackageclasses ''
-flattenpackagehierarchy ''

-adaptclassstrings
-adaptresourcefilecontents **.properties,**.xml,**.yml

# Keep Bukkit main class
-keep class com.emblems.EmblemsPlugin { *; }

# Keep Bukkit API classes (provided at runtime)
-keep class org.bukkit.** { *; }
-keep class net.milkbowl.** { *; }
-keep class me.clip.placeholderapi.** { *; }
-keep class xyz.refinedev.phoenix.** { *; }

# Keep Bukkit reflection targets
-keepnames class * extends org.bukkit.plugin.java.JavaPlugin
-keepclassmembers class * extends org.bukkit.plugin.java.JavaPlugin {
    public <init>(...);
}

# Keep Listener implementations
-keep class * implements org.bukkit.event.Listener { *; }

# Keep CommandExecutors
-keep class * implements org.bukkit.command.CommandExecutor { *; }

# Keep TabCompleters
-keep class * implements org.bukkit.command.TabCompleter { *; }

# Keep InventoryHolders
-keep class * implements org.bukkit.inventory.InventoryHolder { *; }

# Aggressive obfuscation
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepparameternames
-adaptresourceclassmembers **.properties,**.xml,**.yml

-optimizationpasses 5
-dontpreverify
-dontwarn
-mergeinterfacesaggressively
