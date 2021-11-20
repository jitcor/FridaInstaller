Java.perform(function () {
    Java.use('com.github.h.f.installer.FridaApp').checkFridaActive.implementation = function () {
        return true;
    }
});