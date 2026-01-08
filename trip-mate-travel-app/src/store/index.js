import Vue from "vue"
import Vuex from "vuex"
import Cookies from "js-cookie"

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: null,
    token: Cookies.get("token") || null,
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
    },
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        Cookies.set("token", token, { expires: 7 })
      } else {
        Cookies.remove("token")
      }
    },
  },
  actions: {
    login({ commit }, { user, token }) {
      commit("SET_USER", user)
      commit("SET_TOKEN", token)
    },
    logout({ commit }) {
      commit("SET_USER", null)
      commit("SET_TOKEN", null)
    },
  },
  getters: {
    isLoggedIn: (state) => !!state.token,
    currentUser: (state) => state.user,
  },
})
