import * as event from "eventemitter2"
import * as localforage from "localforage"
import * as Post from "./model/post"

type Events = {
  "add": Post[],
  "remove": Post[],
  "reflesh": Post[],
  "error": string
}
let idCounter = 0

class PostStore {
  eventEmitter = new event.EventEmitter2()
  posts = [] as Post[]
  describe<T extends keyof Events>(eventName: T, observer: (t: Events[T]) => void) {
    this.eventEmitter.addListener(eventName, observer)
  }
  static generateId() {
    return ++idCounter
  }
  load() {
    localforage.getItem<Post[]>("posts")
      .then((p) => {
        idCounter = p.reduce((r, p) => Math.max(p.id, r), 0)
        this.reflesh(p || [])
      })
      .catch((r) => this.eventEmitter.emit("error", r))
  }
  add(post: Post) {
    const posts = this.posts.concat(post)
    this.reflesh(posts)
  }
  reflesh(posts: Post[]) {
    localforage.setItem("posts", posts)
      .then(() => this.posts = posts)
      .then(() => this.eventEmitter.emit("reflesh", posts))
      .catch((r) => this.eventEmitter.emit("error", r))
  }
  remove(id: Post.Id) {
    const n = this.posts.findIndex((p) => p.id === id)
    if (n >= 0) {
      const posts = this.posts.slice(0, n).concat(this.posts.slice(n + 1))
      this.reflesh(posts)
    }
  }
}

export default PostStore