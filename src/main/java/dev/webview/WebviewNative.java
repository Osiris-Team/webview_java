package dev.webview;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.ptr.PointerByReference;

import co.casterlabs.rakurai.io.IOUtil;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface WebviewNative extends Library {
    static final PointerByReference NULL_PTR = null;

    @SneakyThrows
    static void runSetup() {
        String[] libraries = null;

        switch (Webview.PLATFORM) {
            case LINUX: {
                libraries = new String[] {
                        "libwebview.so"
                };
                break;
            }

            case MACOSX: {
                libraries = new String[] {
                        "libwebview.dylib"
                };
                break;
            }

            case WINDOWS: {
                libraries = new String[] {
                        "webview.dll",
                        "WebView2Loader.dll"
                };
                break;
            }
        }

        // Extract all of the libs.
        for (String lib : libraries) {
            File file = new File(lib);

            if (!file.exists()) {
                InputStream in = WebviewNative.class.getResourceAsStream("/" + lib);
                byte[] bytes = IOUtil.readInputStreamBytes(in);
                Files.write(file.toPath(), bytes);
            }
        }
    }

    static final int WV_HINT_NONE = 0;
    static final int WV_HINT_MIN = 1;
    static final int WV_HINT_MAX = 2;
    static final int WV_HINT_FIXED = 3;

    /**
     * Used in {@link webview_bind}
     */
    static interface BindCallback extends Callback {

        /**
         * @param seq The request id, used in {@link webview_return}
         * @param req The javascript arguments converted to a json array (string)
         * @param arg Unused
         */
        void callback(long seq, String req, long arg);

    }

    /**
     * Creates a new webview instance. If debug is true - developer tools will be
     * enabled (if the platform supports them). Window parameter can be a pointer to
     * the native window handle. If it's non-null - then child WebView is embedded
     * into the given parent window. Otherwise a new window is created. Depending on
     * the platform, a GtkWindow, NSWindow or HWND pointer can be passed here.
     * 
     * @param debug   Enables developer tools if true (if supported)
     * @param $window A pointer to a native window handle, for embedding the webview
     *                in a window. (Either a GtkWindow, NSWindow, or HWND pointer)
     */
    long webview_create(boolean debug, PointerByReference window);

    /**
     * @return            a native window handle pointer.
     * 
     * @param    $pointer The instance pointer of the webview
     * 
     * @implNote          This is either a pointer to a GtkWindow, NSWindow, or
     *                    HWND.
     */
    long webview_get_window(long $pointer);

    /**
     * Navigates to the given URL.
     * 
     * @param $pointer The instance pointer of the webview
     * @param url      The target url, can be a data uri.
     */
    void webview_navigate(long $pointer, String url);

    /**
     * Sets the title of the webview window.
     * 
     * @param $pointer The instance pointer of the webview
     * @param title
     */
    void webview_set_title(long $pointer, String title);

    /**
     * Updates the webview's window size, see {@link WV_HINT_NONE},
     * {@link WV_HINT_MIN}, {@link WV_HINT_MAX}, and {@link WV_HINT_FIXED}
     * 
     * @param $pointer The instance pointer of the webview
     * @param width
     * @param height
     * @param hint
     */
    void webview_set_size(long $pointer, int width, int height, int hint);

    /**
     * Runs the main loop until it's terminated. You must destroy the webview after
     * this method returns.
     * 
     * @param $pointer The instance pointer of the webview
     */
    void webview_run(long $pointer);

    /**
     * Destroys a webview and closes the native window.
     * 
     * @param $pointer The instance pointer of the webview
     */
    void webview_destroy(long $pointer);

    /**
     * Stops the webview loop, which causes {@link #webview_run(long)} to return.
     * 
     * @param $pointer The instance pointer of the webview
     */
    void webview_terminate(long $pointer);

    /**
     * Evaluates arbitrary JavaScript code asynchronously.
     * 
     * @param $pointer The instance pointer of the webview
     * @param js       The script to execute
     */
    void webview_eval(long $pointer, @NonNull String js);

    /**
     * Injects JavaScript code at the initialization of the new page.
     * 
     * @implSpec          It is guaranteed to be called before window.onload.
     * 
     * @param    $pointer The instance pointer of the webview
     * @param    js       The script to execute
     */
    void webview_init(long $pointer, @NonNull String js);

    /**
     * Binds a native callback so that it will appear under the given name as a
     * global JavaScript function. Internally it uses webview_init().
     * 
     * @param $pointer The instance pointer of the webview
     * @param name     The name of the function to be exposed in Javascript
     * @param callback The callback to be called
     * @param arg      Unused
     */
    void webview_bind(long $pointer, @NonNull String name, BindCallback callback, long arg);

    /**
     * Remove the native callback specified.
     * 
     * @param $pointer The instance pointer of the webview
     * @param name     The name of the callback
     */
    void webview_unbind(long $pointer, @NonNull String name);

    /**
     * Allows to return a value from the native binding. Original request pointer
     * must be provided to help internal RPC engine match requests with responses.
     * 
     * @param $pointer The instance pointer of the webview
     * @param name     The name of the callback
     * @param isError  Whether or not `result` should be thrown as an exception
     * @param result   The result (in json)
     */
    void webview_return(long $pointer, long seq, boolean isError, String result);

}
