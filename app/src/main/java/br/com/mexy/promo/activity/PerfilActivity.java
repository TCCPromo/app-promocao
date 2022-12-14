package br.com.mexy.promo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import br.com.mexy.promo.R;
import br.com.mexy.promo.adapter.ConquistaAdapter;
import br.com.mexy.promo.adapter.PromocaoAdapter;
import br.com.mexy.promo.api.DataService;
import br.com.mexy.promo.fragment.BottomSheetAppBar;
import br.com.mexy.promo.fragment.PostFragment;
import br.com.mexy.promo.fragment.RankingFragment;
import br.com.mexy.promo.model.Conquista;
import br.com.mexy.promo.model.Departamento;
import br.com.mexy.promo.model.Estabelecimento;
import br.com.mexy.promo.model.Promocao;
import br.com.mexy.promo.model.ResponseUsuario;
import br.com.mexy.promo.model.Usuario;
import br.com.mexy.promo.model.UsuarioConquista;
import br.com.mexy.promo.util.Pontuacao;
import br.com.mexy.promo.util.StaticInstances;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.List;

public class PerfilActivity extends AppCompatActivity {

    private Retrofit retrofit;
    private Usuario usuario = new Usuario();
    private TextView textViewNome;
    private TextView textViewNota;
    private TextView textmensagem;
    private FloatingActionButton floatingActionButton;
    private ImageView imageViewUsuario;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    BottomAppBar bottomAppBar;
    private RecyclerView recyclerConquista;
    private Conquista conquista = new Conquista();
    private List<UsuarioConquista> usuarioConquistas = new ArrayList<>();
    private List<Conquista> conquistas = new ArrayList<>();
    private UsuarioConquista usuarioConquista = new UsuarioConquista();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        bottomAppBar = findViewById(R.id.barPerfil);

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetAppBar bottomSheetAppBar = new BottomSheetAppBar();
                bottomSheetAppBar.show(getSupportFragmentManager(), bottomSheetAppBar.getTag());
            }
        });

        smartTabLayout = findViewById(R.id.viewPagerTab);
        viewPager = findViewById(R.id.viewPager);

        //Configurar adapter para abas
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Postagens", PostFragment.class )
                        .add("Ranking", RankingFragment.class )
                        .create()
        );

        viewPager.setAdapter( adapter );
        smartTabLayout.setViewPager( viewPager );

        textmensagem = findViewById(R.id.textmensagem);
        textViewNome = findViewById(R.id.textViewNome);
        textViewNota = findViewById(R.id.textViewNota);
        imageViewUsuario = findViewById(R.id.imageViewUsuario);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE);
        String res = sharedPreferences.getString("ID_USUARIO", null);

        String token = "Bearer " + res;

        retrofit = new Retrofit.Builder()
                .baseUrl(DataService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        recyclerConquista = findViewById(R.id.recyclerConquista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL,false);
        recyclerConquista.setLayoutManager(layoutManager);

        logado(token);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recuperaUsuarioConquista(token);
            }
        }, 2000);



       // verificaponto(token);

        floatingActionButton = findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ProdutoActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    private void logado(String token) {

        DataService service = retrofit.create(DataService.class);
        final Call<Usuario> usuarioCall = service.logado(token);

        usuarioCall.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) {
                    StaticInstances.usuario = response.body();
                    textViewNome.setText(StaticInstances.usuario.toString());
                    textViewNota.setText(StaticInstances.usuario.getPontuacao().toString());
                    Picasso.get()
                            .load(DataService.BASE_URL + StaticInstances.usuario.getUrlImagem())
                            .error(R.mipmap.ic_logo)
                            .into(imageViewUsuario);
                    Bundle bundle = new Bundle();
                    bundle.putInt("idUsuario", (Integer) StaticInstances.usuario.getId());

                }
            }
            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {

            }
        });
    }
    public void Sair(){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_text), null);
        editor.apply();
    }

    private void recuperaUsuarioConquista(String token) {

        DataService service = retrofit.create(DataService.class);
        final Call<List<UsuarioConquista>> usuarioConquistaCall = service.recuperarUsuarioConquistas(token);

                usuarioConquistaCall.enqueue(new Callback<List<UsuarioConquista>>() {
            @Override
            public void onResponse(Call<List<UsuarioConquista>> call, Response<List<UsuarioConquista>> response) {
                if (response.isSuccessful()) {
                    StaticInstances.usuarioConquistas.clear();
                    StaticInstances.usuarioConquistas.addAll(response.body());
                    usuarioConquistas = StaticInstances.usuarioConquistas;
                    if(usuarioConquistas.isEmpty()){
                        textmensagem.setVisibility(View.VISIBLE);
                    }
                    for (UsuarioConquista a : usuarioConquistas) {
                        recuperaConquistas(a.getConquista(), token);
                    }
                    verificaPonto(token);
                }
            }
            @Override
            public void onFailure(Call<List<UsuarioConquista>> call, Throwable t) {

            }
        });
    }

    private void recuperaConquistas(Integer id, String token) {

        DataService service = retrofit.create(DataService.class);
        final Call<Conquista> conquistaCall = service.recuperaConquista(id);

        conquistaCall.enqueue(new Callback<Conquista>() {
            @Override
            public void onResponse(Call<Conquista> call, Response<Conquista> response) {
                if (response.isSuccessful()) {
                    conquista = response.body();
                    conquistas.add(conquista);
                    ConquistaAdapter adapter2 = new ConquistaAdapter(conquistas);
                    recyclerConquista.setAdapter(adapter2);
                }
            }
            @Override
            public void onFailure(Call<Conquista> call, Throwable t) {
            }
        });
    }

    public void verificaPonto(String token){
        Integer ponto = StaticInstances.usuario.getPontuacao();
        for (int i = 0; i <= ponto; i++) {
            if( i == 50){
                lancaponto(token, 1);
            }else if(i == 120){
                lancaponto(token, 2);
            }else if(i == 320){
                lancaponto(token, 3);
            }else if(i == 520){
                lancaponto(token, 4);
            }else if(i == 920){
                lancaponto(token, 5);
            }
        }
    }

    public void lancaponto(String token, Integer conquista) {

        usuarioConquista.setConquista(conquista);
        usuarioConquista.setUsuario(0);

        DataService service = retrofit.create(DataService.class);
        final Call<UsuarioConquista> usuarioCall = service.registrarConquista(token, usuarioConquista);
        usuarioCall.enqueue(new Callback<UsuarioConquista>() {
            @Override
            public void onResponse(Call<UsuarioConquista> call, Response<UsuarioConquista> response) {
                if (response.isSuccessful()) {
                    UsuarioConquista usuarioConquista = response.body();
                    if(usuarioConquista.getConquista() == 1){
                        Pontuacao.alteraponto(retrofit, token, 50);
                    }else{
                        Pontuacao.alteraponto(retrofit, token, 20);
                    }

                }
            }
            @Override
            public void onFailure(Call<UsuarioConquista> call, Throwable t) {
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }
}
