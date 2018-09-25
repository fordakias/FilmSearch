package gr.unipi.msdn.filmsearch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchMovieActivity extends SideBarMenu {

    ListView listView;
    ArrayList<MoviesDataModel> searchList;
    ProgressBar progressBar;
    TextView textMessageSearch;

    // Firebase
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressmain);
        // Connection Firebase
        mAuth = FirebaseAuth.getInstance();
        searchList = new ArrayList<>();

        listView = (ListView) findViewById(R.id.toplistmovies);
        getMoviesSearch("");
        textMessageSearch = (TextView) findViewById(R.id.searchmessage);

        SideBarMenu(R.id.toplistmovieslayout, R.id.nav_view);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getMoviesSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginActivity);
        }
    }

    private void getMoviesSearch(String q) {

        // Create Connection get ApiSearch Result
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiSearch.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        searchList.clear();

        // API Interface
        ApiSearch apiSearch = retrofit.create(ApiSearch.class);

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        apiSearch.getMoviesSearch(q).enqueue(new Callback<MoviesDataModel>() {
            @Override
            public void onResponse(Call<MoviesDataModel> call, Response<MoviesDataModel> response) {
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (response.isSuccessful()) {
                    final MoviesDataModel topMovies = response.body();
                    for (int i = 0; i < topMovies.getResults().size(); i++) {
                        searchList.add(new MoviesDataModel(
                                        topMovies.getPage(),
                                        topMovies.getTotalResults(),
                                        topMovies.getTotalPages(),
                                        topMovies.getResults()
                                )
                        );

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String voteCount = searchList.get(position).getResults().get(position).getVoteCount().toString();
                                String idm = searchList.get(position).getResults().get(position).getId().toString();
                                String video = searchList.get(position).getResults().get(position).getVideo().toString();
                                String voteAverage = searchList.get(position).getResults().get(position).getVoteAverage().toString();
                                String title = searchList.get(position).getResults().get(position).getTitle().toString();
                                String popularity = searchList.get(position).getResults().get(position).getPopularity().toString();
                                String posterPath = searchList.get(position).getResults().get(position).getPosterPath().toString();
                                String originalLanguage = searchList.get(position).getResults().get(position).getOriginalLanguage().toString();
                                String originalTitle = searchList.get(position).getResults().get(position).getOriginalTitle().toString();
                                String backdropPath = searchList.get(position).getResults().get(position).getBackdropPath().toString();
                                String adult = searchList.get(position).getResults().get(position).getAdult().toString();
                                String overview = searchList.get(position).getResults().get(position).getOverview().toString();
                                String releaseDate = searchList.get(position).getResults().get(position).getReleaseDate().toString();

                                Bundle bundle = new Bundle();
                                bundle.putString("VOTE_COUNT", voteCount);
                                bundle.putString("IDM", idm);
                                bundle.putString("VIDEO", video);
                                bundle.putString("VOTEAVERAGE", voteAverage);
                                bundle.putString("TITLE", title);
                                bundle.putString("POPULARITY", popularity);
                                bundle.putString("POSTERPATH", posterPath);
                                bundle.putString("ORIGINALLANGUAGE", originalLanguage);
                                bundle.putString("ORIGINALTITLE", originalTitle);
                                bundle.putString("BACKDROPPATH", backdropPath);
                                bundle.putString("ADULT", adult);
                                bundle.putString("OVERVIEW", overview);
                                bundle.putString("RELEASEDATE", releaseDate);
                                Intent displayMovie = new Intent(SearchMovieActivity.this, DisplayMovie.class);
                                displayMovie.putExtras(bundle);
                                startActivity(displayMovie);
                            }
                        });
                    }
                } else {
                    Log.e("Fail:", "Fail" + response.code());
                }
                AdapterJsonMovies adapter = new AdapterJsonMovies(getApplicationContext(), R.layout.list_movie_layout, searchList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<MoviesDataModel> call, Throwable t) {
                Log.e("ERROR", "ERROR MESSAGE:" + t.getMessage());
            }
        });
    }
}
