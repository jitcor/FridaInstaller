Java.perform(function () {

    function tryCode(enable, func) {
        if (!enable) {
            return
        }
        try {
            func()
        } catch (e) {
            Java.use("android.util.Log").e("FridaLog", e)
        }

    }
    tryCode(true,function (){
        const String = Java.use('java.lang.String')
        // @ts-ignore
        Java.use("com.github.humenger.frida.installer.AboutActivity").onCreate.implementation = function (bundle) {
            this.onCreate(bundle)
            Java.use("android.app.AlertDialog$Builder").$new(this)
                .setTitle(String.$new("Hook tips"))
                .setMessage(String.$new("Hook success"))
                .setPositiveButton(String.$new("OK"), null)
                .show();

        }
    })
    Java.use("android.util.Log").e("FridaLog", "Hook Success")
});