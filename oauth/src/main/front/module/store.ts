import * as event from "eventemitter2"
import * as localforage from "localforage"
import * as Post from "./model/post"
import axios from "axios"
type Events = {
  "add": Post[],
  "remove": Post[],
  "reflesh": Post[],
  "error": string
}

class PostStore {
  eventEmitter = new event.EventEmitter2()
  posts = [] as Post[]
  subscribe<T extends keyof Events>(eventName: T, observer: (t: Events[T]) => void) {
    this.eventEmitter.addListener(eventName, observer)
  }
  load() {
    localforage.getItem<Post[]>("posts")
      .then((p) => {
        const posts = p || []
        this.reflesh(posts)
      })
      .catch((r) => this.eventEmitter.emit("error", r))
  }
  add(content: Post.Content) {
    axios.post("/post", { content }).then((res) => {
      this.reflesh(this.posts.concat(res.data))
    })
  }
  reflesh(posts: Post[]) {
    localforage.setItem("posts", posts)
      .then(() => this.posts = posts)
      .then(() => this.eventEmitter.emit("reflesh", posts))
      .catch((r) => this.eventEmitter.emit("error", r))
  }
  delete(id: Post.Id) {
    const n = this.posts.findIndex((p) => p.id === id)
    if (n >= 0) {
      const post = this.posts[n]
      const posts = this.posts.slice(0, n).concat(this.posts.slice(n + 1))
      axios.delete(`/post/${post.id}`)
        .then(() => {
          this.reflesh(posts)
        })
    }
  }
}

export default PostStore