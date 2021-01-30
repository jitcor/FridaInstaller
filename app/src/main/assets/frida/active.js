Java.perform(function () {
    Java.use('com.github.ihbing.frida.installer.FridaApp').getActiveXposedVersion.implementation = function () {
        return 89;
    }
});