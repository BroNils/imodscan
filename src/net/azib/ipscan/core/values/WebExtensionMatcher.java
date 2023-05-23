package net.azib.ipscan.core.values;

import java.util.*;

/**
 * Web extension matcher.
 *
 * @author GoogleX
 */
public class WebExtensionMatcher extends ObservableArrayList<Map<String, String>> {
	public WebExtensionMatcher() {
		super();
	}

	public static String Serialize(WebExtensionMatcher dataList) {
		StringBuilder sb = new StringBuilder();
		for (Map<String, String> map : dataList) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
			sb.append(";");
		}

		return sb.toString();
	}

	public static WebExtensionMatcher Deserialize(String serializedData) {
		WebExtensionMatcher dataList = new WebExtensionMatcher();
		String[] mapStrings = serializedData.split(";");
		for (String mapString : mapStrings) {
			Map<String, String> map = new HashMap<>();
			String[] keyValuePairs = mapString.split(",");
			for (String keyValuePair : keyValuePairs) {
				int index = keyValuePair.indexOf("=");
				if (index != -1) {
					String key = keyValuePair.substring(0, index);
					String value = keyValuePair.substring(index + 1);
					map.put(key, value);
				}
			}

			if (!map.isEmpty())
			{
				dataList.add(map);
			}
		}

		return dataList;
	}

	public String serialize(){
		return Serialize(this);
	}

	public boolean deserialize(String serializedData){
		this.clear();

		WebExtensionMatcher dataList = Deserialize(serializedData);
		this.addAll(dataList);

		return true;
	}

	public int indexOfName(String name) {
		for (int i = 0; i < size(); i++) {
			if (get(i).get("name").equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Moves the item up in the list.
	 * @param index
	 * @return the new index of the item, or -1 if not found
	 */
	public int moveUpByIndex(int index) {
		if (index <= 0) return index;

		Map<String, String> map = get(index);
		Map<String, String> prevMap = super.set(index - 1, map);
		super.set(index, prevMap);
		return index - 1;
	}

	/**
	 * Moves the item up in the list.
	 * @param name
	 * @return the new index of the item, or -1 if not found
	 */
	public int moveUpByName(String name) {
		int index = indexOfName(name);
		if (index <= 0) return index;

		return moveUpByIndex(index);
	}

	/**
	 * Moves the item down in the list.
	 * @param index
	 * @return the new index of the item, or -1 if not found
	 */
	public int moveDownByIndex(int index) {
		if (index < 0 || index >= size() - 1) return index;

		Map<String, String> map = get(index);
		Map<String, String> nextMap = super.set(index + 1, map);
		super.set(index, nextMap);
		return index + 1;
	}

	/**
	 * Moves the item down in the list.
	 * @param name
	 * @return the new index of the item, or -1 if not found
	 */
	public int moveDownByName(String name) {
		int index = indexOfName(name);
		if (index < 0 || index >= size() - 1) return index;

		return moveDownByIndex(index);
	}

	/**
	 * Build list of names
	 * @return the names of the items in the list
	 */
	public String[] getNames() {
		String[] names = new String[size()];
		for (int i = 0; i < size(); i++) {
			names[i] = get(i).get("name");
		}
		return names;
	}

	public int isNameExist(String name) {
		for (Map<String, String> m : this) {
			if (m.get("name").equals(name)) {
				return indexOf(m);
			}
		}

		return -1;
	}

	// modify to detect duplicates
	@Override
	public Map<String, String> set(int index, Map<String, String> element){
		Map<String, String> old = get(index);

		if (element.isEmpty()) return old;
		if (element.containsKey("name") && element.get("name").isEmpty()) return old;
		if (element.containsKey("matcher") && element.get("matcher").isEmpty()) return old;

		int existingIndex = isNameExist(element.get("name"));
		if (existingIndex >= 0) {
			// override the existing not old (selected index)
			return super.set(existingIndex, element);
		}

		return super.set(index, element);
	}

	@Override
	public boolean add(Map<String, String> map) {
		if (map.isEmpty()) return false;
		if (map.containsKey("name") && map.get("name").isEmpty()) return false;
		if (map.containsKey("matcher") && map.get("matcher").isEmpty()) return false;
		if (isNameExist(map.get("name")) >= 0) return false;

		return super.add(map);
	}

	@Override
	public void add(int index, Map<String, String> map) {
		if (map.isEmpty()) return;
		if (map.containsKey("name") && map.get("name").isEmpty()) return;
		if (map.containsKey("matcher") && map.get("matcher").isEmpty()) return;
		if (isNameExist(map.get("name")) >= 0) return;

		super.add(index, map);
	}

	@Override
	public boolean addAll(Collection<? extends Map<String, String>> e) {
		for (Map<String, String> map : e) {
			int existingIndex = isNameExist(map.get("name"));
			if (existingIndex >= 0) {
				set(existingIndex, map);
				e.remove(map);
			}
		}
		return super.addAll(e);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Map<String, String>> c) {
		for (Map<String, String> map : c) {
			int existingIndex = isNameExist(map.get("name"));
			if (existingIndex >= 0) {
				set(existingIndex, map);
				c.remove(map);
			}
		}
		return super.addAll(index, c);
	}
}
