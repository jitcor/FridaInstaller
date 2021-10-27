Java.perform(function () {
    Java.use('com.github.ihbing.frida.installer.FridaApp').checkFridaActive.implementation = function () {
        return true;
    }
});