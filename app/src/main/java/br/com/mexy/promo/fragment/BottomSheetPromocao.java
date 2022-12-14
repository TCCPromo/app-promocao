package br.com.mexy.promo.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import br.com.mexy.promo.R;
import br.com.mexy.promo.activity.MainActivity;
import br.com.mexy.promo.activity.ProdutoActivity;
import br.com.mexy.promo.activity.PromocaoCompletaActivity;
import br.com.mexy.promo.activity.PromocaoListActivity;
import br.com.mexy.promo.adapter.PromocaoAdapter;
import br.com.mexy.promo.api.DataService;
import br.com.mexy.promo.model.Estabelecimento;
import br.com.mexy.promo.model.Promocao;
import br.com.mexy.promo.util.RecyclerItemClickListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BottomSheetPromocao extends BottomSheetDialogFragment {

    private ProgressBar progressBar;
    private Estabelecimento estabelecimento;
    private Integer idEstabelecimento;
    private Retrofit retrofit;
    private RecyclerView recyclerEstabelecimento;
    private FragmentActivity myContext;
    private ArrayList<Promocao> promocoes;

    @Nullable
    @Override public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        idEstabelecimento = getArguments().getInt("idEstabelecimento");

        return inflater
                .inflate(R.layout.bottom_sheet_promocao, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);

        retrofit = new Retrofit.Builder()
                .baseUrl(DataService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        recyclerEstabelecimento = view.findViewById(R.id.recyclerEstabelecimentos);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                myContext, LinearLayoutManager.HORIZONTAL,false);
        recyclerEstabelecimento.setLayoutManager(layoutManager);

        recuperarEstabelecimentoPromocao(idEstabelecimento);

        recyclerEstabelecimento.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        myContext,
                        recyclerEstabelecimento,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                iniciarActivityPromocaoCompleta(view, position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                iniciarActivityPromocaoCompleta(view, position);
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    private void iniciarActivityPromocaoCompleta(View view, int position) {
        Intent intent = new Intent(myContext, PromocaoCompletaActivity.class);
        intent.putExtra("promocao", promocoes.get(position));
        startActivity(intent);
    }


    private void recuperarEstabelecimentoPromocao(Integer id) {

        DataService service = retrofit.create(DataService.class);
        Call<Estabelecimento> estabelecimentoCall = service.buscarPromocoes(id);

        estabelecimentoCall.enqueue(new Callback<Estabelecimento>() {
            @Override
            public void onResponse(Call<Estabelecimento> call, Response<Estabelecimento> response) {
                if (response.isSuccessful()) {
                    estabelecimento = response.body();
                    promocoes = estabelecimento.getPromocoes();
                    System.out.println("TESTE ESTA: "+ estabelecimento.getNome());
                    PromocaoAdapter adapter = new PromocaoAdapter(promocoes, estabelecimento);
                    recyclerEstabelecimento.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Estabelecimento> call, Throwable t) {
                // TODO
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext=(MainActivity) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            myContext=(MainActivity) activity;
        }
    }

}