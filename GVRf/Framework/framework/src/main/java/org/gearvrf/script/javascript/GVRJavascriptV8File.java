package org.gearvrf.script.javascript;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gearvrf.utility.Log;

import org.gearvrf.GVRContext;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.Bindings;

import lu.flier.script.V8ScriptEngineFactory;


/**
 * Represents a Javascript file that will be compiled and run by the V8 engine
 *
 * Once a script text is set or loaded, you can invoke functions in the
 * script using nvokeFunction(String functionName, Object[] parameters)},
 * to handle events delivered to it.
 */

public class GVRJavascriptV8File {

    /**
     * Loads a Javascript file from a text string.
     *
     * @param gvrContext
     *     The GVR Context.
     * @param scriptText
     *     String containing a Javascript program.
     */
    private static final String TAG = GVRJavascriptV8File.class.getSimpleName();

    protected String mScriptText;
    protected GVRContext mGvrContext = null;
    static protected ScriptEngine mEngine = null;
    protected Bindings bindings = null;
    protected Invocable invocable = null;
    //protected Bindings inputBindings = null;
    static protected Bindings inputBindings = null;
    private Map inputVars = null;
    static private String externalImportStatement = null;


    public GVRJavascriptV8File(GVRContext gvrContext, String scriptText) {

        mScriptText = scriptText;
        //mScriptText = externalImportStatement + scriptText;
        mGvrContext = gvrContext;

        GVRContext.addResetOnRestartHandler(new Runnable() {
            @Override
            public void run() {
                mEngine = null;
            }
        });
    }
    public GVRJavascriptV8File(GVRContext gvrContext) {
        mGvrContext = gvrContext;
        if ( mEngine == null ) {
            mEngine = new V8ScriptEngineFactory().getScriptEngine();
        }
    }

    public void setExternalImportStatement(String externalImportStatement) {
        this.externalImportStatement = externalImportStatement;
        Log.e("X3DDBG", "GVRJavascriptV8File::setExternalImportStatement  externalImportStatement = " + externalImportStatement);
    }


    public String buildImportStatement(String scriptText) {
        mScriptText = this.externalImportStatement + scriptText;
        Log.e("X3DDBG", "GVRJavascriptV8File::buildImportStatement " + mScriptText);
        return mScriptText;
    }

/*
    public void displayInputBindings () {
        Log.e("X3DDBG", "GVRJavascriptV8File::displayInputBindings" );
        Log.e("X3DDBG", "   inputBindings.size(): " + inputBindings.size() );
        Log.e("X3DDBG", "   inputBindings.containsKey(player): " + inputBindings.containsKey("player") +
                "; value: " + inputBindings.get("player").toString() );
    }
    */

    public ScriptEngine getScriptEngine() {
        return mEngine;
    }

    public void setInputValues(Map inputValues) {
        inputVars = inputValues;
    }

    public void setInputValuesAndBindings(Map inputValues) {
        inputVars = inputValues;
        if ( inputVars != null ) {
            //Bindings inputBindings = mEngine.createBindings();
            if (inputBindings == null) inputBindings = mEngine.createBindings();
            inputBindings.putAll(inputVars);
            Log.e("X3DDBG", "GVRJavascriptV8File::setInputValuesAndBindings" );
        }
    }



    public boolean invokeFunction(String funcName, Object[] parameters, String paramString) {
        boolean runs = false;
        try {
            if ( mEngine == null ) {
                mEngine = new V8ScriptEngineFactory().getScriptEngine();
            }
            Log.e("X3DDBG", "GVRJavascriptV8File::invokeFunction BEFORE inputVars != null" );
            //displayInputBindings();
            if ( inputVars != null ) {
                //Bindings inputBindings = mEngine.createBindings();
                if (inputBindings == null) inputBindings = mEngine.createBindings();
                inputBindings.putAll(inputVars);
            }
            Log.e("X3DDBG", "GVRJavascriptV8File::invokeFunction AFTER inputVars != null" );
            //displayInputBindings();

            mEngine.eval( paramString );
            mEngine.eval( mScriptText );

            invocable = (Invocable) mEngine;
            Log.e("X3DDBG", "GVRJavascriptV8File::BEFORE invocable.invokeFunction" );
            //displayInputBindings();
            invocable.invokeFunction(funcName, parameters);
            Log.e("X3DDBG", "GVRJavascriptV8File::AFTER invocable.invokeFunction" );
            //Object returnObject = invocable.invokeFunction(funcName, parameters);
            //Log.e("X3DDBG", "GVRJavascriptV8File::AFTER invocable.invokeFunction, returnObject=" + returnObject.toString());
            //displayInputBindings();
            bindings = mEngine.getBindings( ScriptContext.ENGINE_SCOPE);
            runs = true;
        } catch (ScriptException e) {
            Log.d(TAG, "ScriptException: " + e);
            Log.d(TAG, "   function: '" + funcName + "'");
            Log.d(TAG, "   input Variables: '" + paramString + "'");
            Log.d(TAG, "   JavaScript:\n" + mScriptText);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
            Log.d(TAG, "   function: '" + funcName + "'");
            Log.d(TAG, "   input Variables: '" + paramString + "'");
            Log.d(TAG, "   JavaScript:\n" + mScriptText);
        }
        return runs;
    }

    /**
     * Access to values modified during invoking of Script file
     * Enables X3D to get values script modifies..
     * @return The binding of the current ScriptContext
     */
    public Bindings getLocalBindings() {
        return bindings;
    }

    /**
     * Sets the script file.
     */
    public void setScriptText(String scriptText) {
        mScriptText = scriptText;
    }

    /**
     * Gets the script file.
     * @return The script string.
     */
    public String getScriptText() {
        return mScriptText;
    }
}
