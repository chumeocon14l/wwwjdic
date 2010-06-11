package org.nick.wwwjdic.history;

import org.nick.wwwjdic.R;
import org.nick.wwwjdic.Radical;
import org.nick.wwwjdic.Radicals;
import org.nick.wwwjdic.SearchCriteria;
import org.nick.wwwjdic.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class HistoryItem extends LinearLayout implements
        OnCheckedChangeListener {

    static interface FavoriteStatusChangedListener {
        void onStatusChanged(boolean isFavorite, int id);
    }

    private int id;
    private TextView isKanjiText;
    private TextView searchKeyText;
    private TextView criteriaDetailsText;
    private CheckBox starCb;

    private FavoriteStatusChangedListener favoriteStatusChangedListener;

    HistoryItem(Context context,
            FavoriteStatusChangedListener statusChangedListener) {
        super(context);
        this.favoriteStatusChangedListener = statusChangedListener;

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.search_history_item, this);

        isKanjiText = (TextView) findViewById(R.id.is_kanji);
        searchKeyText = (TextView) findViewById(R.id.search_key);
        criteriaDetailsText = (TextView) findViewById(R.id.criteria_details);
        starCb = (CheckBox) findViewById(R.id.star);
        starCb.setOnCheckedChangeListener(this);
    }

    public void populate(SearchCriteria criteria) {
        id = criteria.getId();
        isKanjiText.setText(criteria.isKanjiLookup() ? "��" : "��");
        String searchKey = criteria.getQueryString();
        if (criteria.isKanjiRadicalLookup()) {
            Radical radical = Radicals.getInstance().getRadicalByNumber(
                    Integer.parseInt(searchKey));
            searchKey = radical.getGlyph() + " (" + radical.getNumber() + ")";
        }
        searchKeyText.setText(searchKey);

        String detailStr = buildDetailString(criteria);
        if (detailStr != null && !"".equals(detailStr)) {
            criteriaDetailsText.setText(detailStr);
        }

        starCb.setOnCheckedChangeListener(null);
        starCb.setChecked(criteria.isFavorite());
        starCb.setOnCheckedChangeListener(this);
    }

    private String buildDetailString(SearchCriteria criteria) {
        StringBuffer buff = new StringBuffer();

        if (criteria.isKanjiLookup()) {
            String kanjiSearchName = lookupKanjiSearchName(criteria);

            buff.append(kanjiSearchName);
            if (criteria.hasStrokes()) {
                buff.append(" (strokes: ");
                if (criteria.hasMinStrokes()) {
                    buff.append(criteria.getMinStrokeCount());
                }
                buff.append("-");
                if (criteria.hasMaxStrokes()) {
                    buff.append(criteria.getMaxStrokeCount());
                }
                buff.append(")");
            }
        } else {
            String dictName = lookupDictionaryName(criteria);

            buff.append(dictName);

            String dictOptStr = buildDictOptionsString(criteria);
            if (!StringUtils.isEmpty(dictOptStr)) {
                buff.append(" ");
                buff.append(dictOptStr);
            }
        }

        String result = buff.toString();
        if (!StringUtils.isEmpty(result)) {
            return result.trim();
        }

        return result;
    }

    private String buildDictOptionsString(SearchCriteria criteria) {
        StringBuffer buff = new StringBuffer();
        if (criteria.isCommonWordsOnly()) {
            buff.append(" comm.");
        }

        if (criteria.isExactMatch()) {
            buff.append(" ex.");
        }

        if (criteria.isRomanizedJapanese()) {
            buff.append(" rom.");
        }

        String result = buff.toString().trim();
        if (!StringUtils.isEmpty(result)) {
            result = "(" + result + ")";
        }

        return result;
    }

    private String lookupDictionaryName(SearchCriteria criteria) {
        String dictCode = criteria.getDictionary();
        String dictName = dictCode;

        String[] dictCodes = getResources().getStringArray(
                R.array.dictionary_codes_array);
        String[] dictNames = getResources().getStringArray(
                R.array.dictionaries_array);
        int idx = linearSearch(dictCode, dictCodes);

        if (idx != -1 && idx < dictNames.length - 1) {
            dictName = dictNames[idx];
        }
        return dictName;
    }

    private String lookupKanjiSearchName(SearchCriteria criteria) {
        String kanjiSearchCode = criteria.getKanjiSearchType();
        String kanjiSearchName = kanjiSearchCode;

        String[] searchCodes = getResources().getStringArray(
                R.array.kanji_search_codes_array);
        String[] searchNames = getResources().getStringArray(
                R.array.kanji_search_types_array);
        int idx = linearSearch(kanjiSearchCode, searchCodes);

        if (idx != -1 && idx < searchNames.length - 1) {
            kanjiSearchName = searchNames[idx];
        }
        return kanjiSearchName;
    }

    private int linearSearch(String key, String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            String code = arr[i];
            if (code.equals(key)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        favoriteStatusChangedListener.onStatusChanged(isChecked, id);
    }
}
