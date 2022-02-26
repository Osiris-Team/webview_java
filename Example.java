package dev.webview;

public class Example {

    public static void main(String[] args) {
        Webview wv = new Webview(); // Can optionally be created with an AWT component to be painted on.

        // Calling `await echo(1,2,3)` will return `[1,2,3]`
        wv.bind("echo", (arguments) -> {
            return arguments;
        });

        wv.loadURL("https://google.com");

        // Run the webview event loop, the webview is fully disposed when this returns.
        wv.run();
    }

}
