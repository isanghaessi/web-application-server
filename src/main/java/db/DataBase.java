package db;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import user.model.User;

public class DataBase {
	private static Map<String, User> users = Maps.newHashMap();

	public static void addUser(User user) {
		users.put(user.getUserId(), user);
	}

	public static User findUserById(String userId) {
		return users.get(userId);
	}

	public static Collection<User> findAll() {
		return users.values();
	}

	public static boolean isRegisteredUser(User user) {
		if (Objects.isNull(findUserById(user.getUserId()))) {
			return false;
		}

		User registeredUser = users.get(user.getUserId());

		return registeredUser.getPassword().equals(user.getPassword());
	}
}
