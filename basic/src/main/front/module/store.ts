import * as event from "eventemitter2"
import * as localforage from "localforage"
import * as Post from "./model/post"
import axios from "axios"
import * as dateFns from "date-fns"
import * as compareAsc from "date-fns/compare_asc"
type Events = {
  "add": Post[],
  "delete": Post[],
  "reflesh": Post[],
  "reflesh:not-saved": Post[],
  "reflesh:saved": Post[],
  "reflesh:delete": Post[],
  "error": string
}

class PostStore {
  eventEmitter = new event.EventEmitter2()

  notSavedPost = [] as Post[]
  savedPost = [] as Post[]
  deletePost = [] as Post[]
  constructor() {
    this.subscribe("reflesh:delete", this.onRefleshDelete)
    this.subscribe("reflesh:not-saved", this.onRefleshNotSaved)
    this.subscribe("reflesh:saved", this.onRefleshSaved)
  }
  subscribe<T extends keyof Events>(eventName: T, observer: (t: Events[T]) => void) {
    this.eventEmitter.addListener(eventName, observer)
  }
  private emit<T extends keyof Events>(eventName: T, item: Events[T]) {
    this.eventEmitter.emit(eventName, item)
  }
  load() {
    localforage.getItem<Post[]>("saved")
      .then((p) => this.emit("reflesh:saved", p))
      .catch((r) => this.eventEmitter.emit("error", r))

    localforage.getItem<Post[]>("not-saved")
      .then((p) => this.emit("reflesh:not-saved", p))
      .catch((r) => this.eventEmitter.emit("error", r))

    localforage.getItem<Post[]>("delete")
      .then((p) => this.emit("reflesh:delete", p))
      .catch((r) => this.eventEmitter.emit("error", r))
  }
  posts() {
    return this.notSavedPost.concat(this.savedPost).concat(this.deletePost).sort((p1, p2) => compareAsc(p1.date, p2.date))
  }
  onRefleshSaved = (savedPost: Post[]) => {
    this.savedPost = savedPost || []
    localforage.setItem("saved", savedPost).then(() =>
      this.emit("reflesh", this.posts()))
  }
  onRefleshNotSaved = (notSavedPost: Post[]) => {
    this.notSavedPost = notSavedPost || []
    localforage.setItem("not-saved", notSavedPost).then(() =>
      this.emit("reflesh", this.posts()))
  }
  onRefleshDelete = (deletePost: Post[]) => {
    this.deletePost = deletePost || []
    localforage.setItem("delete", deletePost).then(() =>
      this.emit("reflesh", this.posts()))
  }

  add(content: Post.Content) {
    const date = dateFns.format(new Date(), "YYYY-MM-DDTHH:mm:ss.SSS")
    const newPost = { content, date };
    const beforeSavedPost = this.notSavedPost
    this.emit("reflesh:not-saved", this.notSavedPost.concat(newPost))
    axios.post("/post", newPost).then((res) => {
      const post: Post = res.data
      this.emit("reflesh:not-saved", beforeSavedPost)
      this.emit("reflesh:saved", this.savedPost.concat(post))
    })
  }
  delete(post: Post) {
    if (post.id !== null) {
      const n = this.savedPost.findIndex((p) => p.id === post.id)
      if (n >= 0) {
        const post = this.savedPost[n]
        const posts = this.savedPost.slice(0, n).concat(this.savedPost.slice(n + 1))
        this.emit("reflesh:saved", posts)
        this.emit("reflesh:delete", this.deletePost.concat(post))
        /*
        axios.delete(`/post/${post.id}`)
          .then(() => {
            this.reflesh(posts)
          })
        */
      }
    } else {
      const n = this.notSavedPost.findIndex((p) => p === post)
      if (n >= 0) {
        const post = this.notSavedPost[n]
        const posts = this.notSavedPost.slice(0, n).concat(this.notSavedPost.slice(n + 1))
        this.emit("reflesh:not-saved", posts)
        this.emit("reflesh:delete", this.deletePost.concat(post))
      }

    }
  }
}

export default PostStore