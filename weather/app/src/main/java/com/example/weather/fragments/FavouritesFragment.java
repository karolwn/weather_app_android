package com.example.weather.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weather.MyArrayAdapter;
import com.example.weather.R;
import com.example.weather.model.FavouritesModel;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Fragment that displays list of favourites cities, it allows to delete selected ones (selection
 * after long click) or to display current weather for a chosen city (normal click) - go to the
 * homeFragment
 */
public class FavouritesFragment extends Fragment implements AdapterView.OnItemLongClickListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String FAVOURITES = "favourites";

    private FragmentFavouritesListener listener;
    private ArrayList<FavouritesModel> listItems;
    private ArrayList<Integer> selectedIndex;
    private MyArrayAdapter myListAdapter;

    private GridLayout delTextBox;
    private Button cancelButton;
    private Button selectAllButton;
    private Button delButton;

    private static String TAG = "obrot";

    private int selectionCounter = 0;

    /**
     * Interface to provide communication between Fragments/Activities through the MainActivity
     */
    public interface FragmentFavouritesListener {

        /**
         * sends data to the activity/class which implements it
         * @param data ArrayList containing favourite cities as objects - type: FavouritesModel
         */
        void sendArrayList(ArrayList<FavouritesModel> data);

        /**
         * sends only a city's name to HomeFragment
         * @param city city's name
         */
        void sendSingleCity(String city, boolean isFavourite);

        /**
         * It checks whether currently displayed on homeFragment city is still favourite after
         * deleting some data by the user for example: home - London, favs - London, Cracow, user
         * deletes London -> we need button update, and that is the job of this method
         * implemented in MainActivity.
         */
        void buttonUpdate();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (selectedIndex == null) {
            selectedIndex = new ArrayList<>();
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional,
     * and non-graphical fragments can return null. This will be called between onCreate(Bundle)
     * and onActivityCreated(Bundle)
     *
     * @param inflater:          The LayoutInflater object that can be used to inflate any
     *                           views in the fragment,
     * @param container:         If non-null, this is the parent view that the fragment's UI should
     *                           be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState : If non-null, this fragment is being re-constructed from
     *                           a previous saved state as given here.
     **/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (listItems == null || listItems.isEmpty()) {
            return inflater.inflate(R.layout.fragment_favourites_empty, container, false);
        } else {
            View view = inflater.inflate(R.layout.fragment_favourites, container, false);

//          "Objects.requireNonNull(getContext())" is the same as "getContext()", but it prevents NullPointerException
            myListAdapter = new MyArrayAdapter(Objects.requireNonNull(getContext()), R.layout.fragment_element_list, listItems);
            ListView listView = view.findViewById(R.id.list_view_list);
            listView.setAdapter(myListAdapter);

            listView.setOnItemLongClickListener(this);
            listView.setOnItemClickListener(this);

            delTextBox = view.findViewById(R.id.favourites_fragment_delete_layout);

            calculateCounter();
            if (selectionCounter > 0) {
                delTextBox.setVisibility(View.VISIBLE);
            }
            cancelButton = view.findViewById(R.id.button_cancel);
            cancelButton.setOnClickListener(this);

            selectAllButton = view.findViewById(R.id.button_select_all);
            selectAllButton.setOnClickListener(this);

            delButton = view.findViewById(R.id.button_delete);
            delButton.setOnClickListener(this);

            return view;
        }
    }


    /**
     * Updates list containing favorite locations, it is called by MainActivity in implementation of
     * FavouritesFragmentListener
     * @param input contains saved locations
     */
    public void updateListItems(ArrayList<FavouritesModel> input) {
        listItems = input;

    }

    /**
     * Calculates the correct value of selectCounter
     * It iterates through the list containing favourite locations, and checks if the particular
     * city is selected. selectionCounter is used to determine if control buttons should be displayed
     */
    private void calculateCounter() {
        for (FavouritesModel element : listItems) {
            if (element.isSelected()) {
                selectionCounter++;
            }
        }
    }

    /**
     * Sets selectionCounter to 0 -> hide control buttons
     * It also deselect everything
     * Used when user leaves the favouriteFragment
     */
    public void resetSelection() {
        selectionCounter = 0;
        for (int i = 0; i < listItems.size(); i++){
            if (listItems.get(i).isSelected()){
                selectedIndex.add(i);
                listItems.get(i).setSelected(false);
            }
        }
    }

    /**
     * Adds new city to the list
     * @param name: name of the new city
     */
    public void addNewCity(String name) {
        boolean isPresent = isCityFavourite(name);

        if (!isPresent) {
            listItems.add(new FavouritesModel(name, false));
        }
    }

    /**
     * Delete city when you have its name, used in MainActivity
     * @param name: name of a city to remove
     */
    public void deleteCityBasedOnName(String name) {
        for (int i = listItems.size() - 1; i >= 0; i--) {
            if (listItems.get(i).getCityName().equals(name)) {
                listItems.remove(i);
            }
        }
    }

    /**
     * Delete city/cities that was/were previously selected by the user.
     */
    private void deleteCityBasedOnSelection(){
        for (int i = listItems.size() - 1; i >= 0; i--){
            if (listItems.get(i).isSelected()){
                listItems.remove(i);
            }
        }
    }

    /**
     * loops through the favourites ArrayList and checks whether given city is already there
     * @param city: to check if it is favourite
     * @return true if found else false;
     */
    public boolean isCityFavourite(String city) {
        if (listItems == null) {
            listItems = new ArrayList<>();
        }
        for (int i = 0; i < listItems.size(); i++){
            if (listItems.get(i).getCityName().toLowerCase().equals(city.toLowerCase())){
                return true;
            }
        }
        return false;
    }


    /**
     * Used in data saving
     * @return ArrayList that contains favourites
     */
    public ArrayList<FavouritesModel> getListItems(){
        return  this.listItems;
    }

    /**
     * used to select or deselect all elements of ArrayList<FavouritesModel> - used when user
     * clicks on "cancel" or "select all" button
     * @param list: list that contains elements to "select" or "deselect"
     * @param newState: new state aka "selected" or "deselected
     */
    public void selectOrDeselectAll(ArrayList<FavouritesModel> list, boolean newState){
        for (FavouritesModel city : list) {
            city.setSelected(newState);
        }
    }

    /**
     * Cancel, select all, delete buttons onClickListener
     * This method handles pressed buttons
     * @param v: pressed button
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel:
                selectOrDeselectAll(listItems, false);
                selectionCounter = 0;
                delTextBox.setVisibility(View.GONE);
                myListAdapter.notifyDataSetChanged();
                break;

            case R.id.button_select_all:
                selectOrDeselectAll(listItems, true);
                selectionCounter = listItems.size();
                myListAdapter.notifyDataSetChanged();
                break;

            case R.id.button_delete:
                deleteCityBasedOnSelection();
                selectionCounter = 0;
                delTextBox.setVisibility(View.GONE);
                listener.buttonUpdate();
                myListAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * It handles quick click on favourite element
     * @param parent: The AdapterView where the click happened.
     * @param view: The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position: The position of the view in the adapter.
     * @param id: The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FavouritesModel model = listItems.get(position);
        listener.sendSingleCity(model.getCityName(), true);
    }

    /**
     * It defines what happens after user's long click on a element of the ListView
     * @param parent: The AbsListView where the click happened, ListView which holds "clickable"
     *               favourite cities
     * @param view: The view within the AbsListView that was clicked, clicked favourite city
     * @param position: The position of the view in the list, position of the favourite city
     * @param id: The row id of the item that was clicked, id of clicked view
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        myListAdapter.notifyDataSetChanged();
        FavouritesModel model = listItems.get(position);
        if (model.isSelected()) {
            model.setSelected(false);
            selectionCounter--;
        } else {
            model.setSelected(true);
            selectionCounter++;
        }

        listItems.set(position, model);

        if (selectionCounter != 0){ // if anything is selected
            delTextBox.setVisibility(View.VISIBLE);
        } else {
            delTextBox.setVisibility(View.GONE);
            myListAdapter.notifyDataSetChanged();
        }
        return true;
    }

    /**
     * Called when a fragment is first attached to its context (Main Activity).
     * onCreate(android.os.Bundle) will be called after this.
     * If you override this method you must call through to the superclass implementation.
     * To ensure that the host activity implements this interface, fragment A's onAttach() callback
     * method (which the system calls when adding the fragment to the activity) instantiates an
     * instance of FragmentFavouritesListener by casting the Activity that is passed into onAttach():
     * @param context: Context
     **/
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //checking whether MainActivity (context) implements FragmentFavouritesListener
        if (context instanceof FragmentFavouritesListener) {
            listener = (FragmentFavouritesListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentFavouritesListener");
        }
    }

    /**
     * Called when the fragment is being disassociated from the activity.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Called when the view hierarchy associated with the fragment is being removed.
     */
    @Override
    public void onPause() {
        super.onPause();
        listener.sendArrayList(listItems);
    }
}