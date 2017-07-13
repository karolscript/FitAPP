/**
 * Created by andres on 17/04/17.
 */

package com.fitucab.ds1617b.fitucab.UI.Fragments.M03;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fitucab.ds1617b.fitucab.Helper.IpStringConnection;
import com.fitucab.ds1617b.fitucab.Helper.ManagePreferences;
import com.fitucab.ds1617b.fitucab.Model.User;
import com.fitucab.ds1617b.fitucab.Model.UserAuxiliar;
import com.fitucab.ds1617b.fitucab.Model.Person;
import com.fitucab.ds1617b.fitucab.Model.UsersAdapter;
import com.fitucab.ds1617b.fitucab.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;

/**
 Este fragmento nos permite ver a los usuarios con y sin la aplicacion en el cual si no tiene la app
 podemos enviarle una solicitud de descarga , tambien si tiene la aplicacion agregar a ese amigo.

 */
public class M03FragmentLibreta extends Fragment {

    ManagePreferences manageId = new ManagePreferences();
    int userId;
    IpStringConnection ipString = new IpStringConnection();

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    ListView listView;
    View rootView;
    /**
     Es llamado cuando el fragmento es creado por primera vez ,devuelve una View (rootView)desde
     este método que será la raíz del diseño de nuestro fragmento

     @param savedInstanceState es un Bundle que proporciona datos acerca de la instancia previa del fragmento.
     @param container Es el ViewGroup principal en el cual se insertará el diseño de nuestro fragmento.
     @param inflater este parametro sirve para 3 argumentos pero aqui solo lo utilizamos para el
                     Viewgroup. Para que el sistema aplique parámetros de diseño a la vista de raíz
                     del diseño agrandado, especificada por la vista principal a la que se integra.
     */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String hasPhone = "";

       //Aqui nos aseguramos que la app solicita los permisos para leer los contactos de la libreta
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_CONTACTS},PERMISSIONS_REQUEST_READ_CONTACTS
            );
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }
        //se encarga de poner los atributos de diseño del ViewGroup padre
        rootView = inflater.inflate(R.layout.fragment_m03_contacts, container, false);
        userId = manageId.getIdUser(rootView.getContext());
        // se instancia un arrraylist
        ArrayList<UserAuxiliar> arrayOfUsers = new ArrayList<UserAuxiliar>();
        //nos proveera los datos del usuario
        final UsersAdapter adapter = new UsersAdapter(rootView.getContext(), arrayOfUsers);
        //Tomamos id de listview desde xml
        listView = (ListView) rootView.findViewById(R.id.contactsList);
        //Establecemos el adaptador que proporciona los datos y las vistas para representar los datos
        listView.setAdapter(adapter);
        //instanciamos un arraylist auxiliar de usuarios
        final ArrayList<UserAuxiliar> usuarios = new ArrayList<UserAuxiliar>();
        //Se llama cuando se ha hecho clic y se ha mantenido una vista y el True porque es el clic largo
        listView.setLongClickable(true);
        // este es el llamado que hacemos para agregar los conexto de bloquear, aceptar declinar
        // Asociamos los menús contextuales a los controles
        registerForContextMenu(listView);

        // obtenemos el ContentResolver
        ContentResolver cr = rootView.getContext().getContentResolver();
        // Obtener el Cursor de todos los contactos
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //Mueva el cursor a la primera posición y Comprueba también si el cursor está vacío o no.
        if (cursor.moveToFirst()) {
            //Iterar a través del cursor
            do {

                // Obtiene los nombre de los contactos y el numero de telefono
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);
                phones.moveToFirst();
                if (phones!=null && phones.moveToFirst())
                    hasPhone = phones.getString(phones.getColumnIndex(Phone.HAS_PHONE_NUMBER));
                String phoneNumber = "0";
                String emailAddress = "";
                if ( hasPhone.equalsIgnoreCase("1"))
                    hasPhone = "true";
                else
                    hasPhone = "false" ;

                if (Boolean.parseBoolean(hasPhone))
                {
                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replaceAll("[()-]","");
                    phoneNumber = phoneNumber.replaceAll(" ","");
                    phones.close();
                }

                //obtenemos el email
                Cursor emails = cr.query(CommonDataKinds.Email.CONTENT_URI, null, CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
                while (emails.moveToNext())
                {
                    emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                }
                emails.close();
                //agregamos al usuario con nombre email y telefono
                usuarios.add(new UserAuxiliar(name,emailAddress, phoneNumber));

              //adapter.addAll(usuarios);
                phones.close();
                emails.close();
            } while (cursor.moveToNext());
        }
        //creamos un objeto gson
        Gson gson = new Gson();
        //Java objeto a JSON, y se lo asignamos a un string
        String contacts = gson.toJson(usuarios);

        usuarios.clear();

        String contactsEncoded = "";
        try {
            contactsEncoded = URLEncoder.encode(contacts, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = ipString.getIp()+"contact/getContacts?id="+userId+"&contacts=" + contactsEncoded;
        final Gson gsonresp = new Gson();
        // Inicializamos el RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(rootView.getContext());
        //Solicitar una respuesta de cadena desde la URL proporcionada.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<User> ap = gsonresp.fromJson(response,new TypeToken<List<User>>(){}.getType());
                        boolean withApp = true;
                        for(int i = 0;i<ap.size();i++) {
                            if (ap.get(i).get_idUser() == -1){
                                usuarios.add(new UserAuxiliar(0, "", 0, 2));
                            }
                            else if (ap.get(i).get_idUser() == -2) {
                                usuarios.add(new UserAuxiliar(0, "", 0, 3));
                                withApp = false;
                            }else if (withApp) {
                                usuarios.add(new UserAuxiliar(ap.get(i).get_idUser(), ap.get(i).get_username(),ap.get(i).get_point(),0));
                            }else
                                usuarios.add(new UserAuxiliar(ap.get(i).get_username(), ap.get(i).get_email(), ap.get(i).get_phone(),1));
                        }
                        adapter.addAll(usuarios);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //dialogo de alerta de un error de conexion
                AlertDialog.Builder builder1 = new AlertDialog.Builder(rootView.getContext());
                builder1.setMessage(R.string.et_03_errorconexion);
                builder1.setCancelable(true);
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        // Agregue la solicitud al RequestQueue.
        queue.add(stringRequest);
        cursor.close();
        return rootView;
    }
    /**
     Este metodo se llama cada vez que se necesita mostrar un menú contextual
     Aquí es donde se definen los elementos del menú

     @param menu define que menu debe inflarce
     @param menuInfo proporciona información adicional sobre el elemento seleccionado
     @param v le pasamos la lista que en nuestro caso es el listview
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        menu.setHeaderTitle(R.string.et_03_menulibreta);
        UserAuxiliar user = (UserAuxiliar) listView.getItemAtPosition(info.position);
        if(user.get_type() == 0){
            menu.add(0, user.get_id(), 0, R.string.btn_03_agregar);
        }
        else if(user.get_type() == 1){
            menu.add(1, user.get_id(), 0, R.string.btn_03_invitar);
        }
    }
    /**
     Cuando el usuario selecciona un elemento de menú, el sistema llama a este método para
     que pueda realizar la acción apropiada
     En nuestro caso identificamos cada uno de los elementos  cuando se  hace la accion
     onContectItemSelected podemos ver si ya existe la amistad , si ya existe una peticion enviada,
     si existe algun error de conexion y enviamos la notificacion para descargar la app
     la respuesta en el super.onContextItemSelected(item)
     @param item le pasamos el item seleccionado

     */

    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        switch (item.getGroupId()) {
            case 0:
                String url = ipString.getIp()+"friend/request?idRequester="+userId+"&idRequested="+Integer.toString(item.getItemId());
                final Gson gson = new Gson();

                // Inicializamos el RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(rootView.getContext());
                // Solicitar una respuesta de cadena desde la URL proporcionada.
                StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.equals(R.string.et_03_yaexisteamistad)){
                                    String urlAccept = ipString.getIp()+"friend/update?idUpdater="+userId+"&idUpdated="+Integer.toString(item.getItemId())+"&Action=Request";
                                    final Gson gsonAccept = new Gson();

                                    // Inicializamos el RequestQueue.
                                    RequestQueue queueUpdate = Volley.newRequestQueue(rootView.getContext());

                                    // Solicitar una respuesta de cadena desde la URL proporcionada.
                                    StringRequest stringRequestUpdate = new StringRequest(Request.Method.POST, urlAccept,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {

                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //dialogo de alerta de un error de conexion
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(rootView.getContext());
                                            builder1.setMessage(R.string.et_03_errorconexion);
                                            builder1.setCancelable(true);
                                            AlertDialog alert11 = builder1.create();
                                            alert11.show();
                                        }
                                    });
                                    queueUpdate.add(stringRequestUpdate);
                                }
                                else{
                                    //dialogo de alerta de una peticion enviada
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(rootView.getContext());
                                    builder1.setMessage(R.string.et_03_peticionenviada);
                                    builder1.setCancelable(true);
                                    AlertDialog alert11 = builder1.create();
                                    alert11.show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //dialogo de alerta de un error de conexion
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(rootView.getContext());
                        builder1.setMessage(R.string.et_03_errorconexion);
                        builder1.setCancelable(true);
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                });
                // agregamos la solicitud al RequestQueue.
                queue.add(stringRequest);

                try {
                    sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getFragmentManager().beginTransaction().detach(this).attach(this).commit();

                try {
                    sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return true;
            case 1:
                    //ENVIAR NOTIFICACION PARA DESCARGAR LA APP
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}

