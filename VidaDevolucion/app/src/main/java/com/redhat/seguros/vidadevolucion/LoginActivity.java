package com.redhat.seguros.vidadevolucion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.redhat.seguros.vidadevolucion.aplicacion.Configuracion;
import com.redhat.seguros.vidadevolucion.view.ProductsActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.tietUsername)
    EditText tietUsername;
    @BindView(R.id.tietPassword)
    EditText tietPassword;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.btnLoginFacebook)
    LoginButton btnLoginFacebook;
    @BindView(R.id.tilUsername)
    TextInputLayout tilUsername;
    @BindView(R.id.tilPassword)
    TextInputLayout tilPassword;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        btnLoginFacebook.setReadPermissions("email","user_birthday");

        callbackManager = CallbackManager.Factory.create();

        btnLoginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Autenticando");
                progressDialog.show();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                String id = object.optString("id");
                                String firstName = object.optString("first_name");
                                String lastName = object.optString("last_name");
                                String email = object.optString("email");
                                String birthday = object.optString("birthday");

                                Toast.makeText(LoginActivity.this, "onSuccess->" + object.toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, ProductsActivity.class);
                                startActivity(intent);
                                finish();
                                progressDialog.dismiss();

                            }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, name, link, email, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btnLogin)
    public void clickLogin(){

        String usuario = tietUsername.getText().toString();
        String contrasena = tietPassword.getText().toString();

        if (usuario.trim().length() == 0){
            Toast.makeText(LoginActivity.this,"Ingrese un Usuario", Toast.LENGTH_SHORT).show();
            //tietUsername.setError("Ingrese un Usuario");
            return;
        }

        if (contrasena.trim().length() == 0){
            Toast.makeText(LoginActivity.this, "Ingrese la Contraseña", Toast.LENGTH_SHORT).show();
            //tietPassword.setError("Ingrese la Contraseña");
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Autenticando");
        progressDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("clave", contrasena);
        params.put("codusuario", usuario.toUpperCase());


        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                "http://www.rimac-seguros.com/AsesorRobocop/services/obtDatosUsuarioAD.do",
                jsonObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject  response) {
                        //Para ver el Cargando
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        System.out.println(response.toString());
                                        Toast.makeText(LoginActivity.this, "onSuccess->" + response.toString(), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, ProductsActivity.class);
                                        startActivity(intent);
                                    }
                                }, 3000);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "onError->" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }, 2000);
                    }
                }
        );
        Configuracion.getInstance().agregarRequestQueue(jsonObjectRequest);

    }

}
