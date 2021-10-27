Java.perform(function () {
    Java.use('com.github.humenger.frida.installer.FridaApp').checkFridaActive.implementation = function () {
        return true;
    }
});