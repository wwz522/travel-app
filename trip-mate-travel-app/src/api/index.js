import request from "@/utils/axios"

export function login(data) {
  return request({
    url: "/auth/login",
    method: "post",
    data,
  })
}

export function register(data) {
  return request({
    url: "/auth/register",
    method: "post",
    data,
  })
}

export function getRoutes(params) {
  return request({
    url: "/content/list",
    method: "get",
    params: { ...params, type: 'route' },
  })
}

export function getRouteDetail(id) {
  return request({
    url: `/content/route/${id}`,
    method: "get",
  })
}

export function createRoute(data) {
  return request({
    url: "/content/route",
    method: "post",
    data,
  })
}

export function getAttractions(params) {
  return request({
    url: "/content/list",
    method: "get",
    params: { ...params, type: 'attraction' },
  })
}

export function getAttractionDetail(id) {
  return request({
    url: `/content/attraction/${id}`,
    method: "get",
  })
}

export function createAttraction(data) {
  return request({
    url: "/content/attraction",
    method: "post",
    data,
  })
}

export function likeContent(data) {
  return request({
    url: "/interaction/like",
    method: "post",
    data,
  })
}

export function unlikeContent(data) {
  return request({
    url: "/interaction/unlike",
    method: "post",
    data,
  })
}

export function favoriteContent(data) {
  return request({
    url: "/interaction/favorite",
    method: "post",
    data,
  })
}

export function unfavoriteContent(data) {
  return request({
    url: "/interaction/unfavorite",
    method: "post",
    data,
  })
}

export function getComments(params) {
  return request({
    url: "/interaction/comments",
    method: "get",
    params,
  })
}

export function addComment(data) {
  return request({
    url: "/interaction/comment",
    method: "post",
    params: {
      targetType: data.targetType,
      targetId: data.targetId
    },
    data: {
      content: data.content
    },
  })
}

export function getMyRoutes() {
  return request({
    url: "/user/posts",
    method: "get",
    params: { type: 'route' }
  })
}

export function getMyAttractions() {
  return request({
    url: "/user/posts",
    method: "get",
    params: { type: 'attraction' }
  })
}

export function getMyFavorites() {
  return request({
    url: "/user/favorites",
    method: "get",
  })
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append("file", file)
  return request({
    url: "/file/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  })
}

export function planRoute(data) {
  return request({
    url: "/ai/plan-route",
    method: "post",
    data,
    timeout: 120000, // AI规划接口单独设置120秒超时
  })
}

export function updateRoute(id, data) {
  return request({
    url: `/content/route/${id}`,
    method: "put",
    data,
  })
}

export function updateAttraction(id, data) {
  return request({
    url: `/content/attraction/${id}`,
    method: "put",
    data,
  })
}

export function getDrafts(params) {
  return request({
    url: "/content/drafts",
    method: "get",
    params,
  })
}

export function deleteComment(id) {
  return request({
    url: `/interaction/comment/${id}`,
    method: "delete",
  })
}

export function likeComment(commentId) {
  return request({
    url: `/interaction/comment/${commentId}/like`,
    method: "post",
  })
}

export function deletePost(id) {
  return request({
    url: `/user/post/${id}`,
    method: "delete",
  })
}

export function getProfile() {
  return request({
    url: "/user/me",
    method: "get",
  })
}

export function updateProfile(data) {
  return request({
    url: "/user/profile",
    method: "put",
    data,
  })
}
