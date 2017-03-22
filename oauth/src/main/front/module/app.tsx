import * as React from "react";
import PostView, { Post } from "./post"
import PostForm from "./post-form"
import * as localforage from "localforage"

type ApplicationState = {
  posts: Post[]
}
class App extends React.Component<never, ApplicationState>{
  state = { posts: [] as Post[] }
  componentDidMount() {
    // TODO error handling
    localforage.getItem<Post[]>("posts").then((posts) => {
      if (posts != null) {
        this.setState({ posts })
      }
    })
  }
  post = (content: string) => {
    const posts = this.state.posts.concat({ content })
    // TODO error handling
    localforage.setItem("posts", posts)
    this.setState({ posts })
  }
  deletePost = (content: string) => {
    const n = this.state.posts.findIndex((x) => x.content === content);
    if (n >= 0) {
      const posts = this.state.posts.slice(0, n).concat(this.state.posts.slice(n + 1))
      localforage.setItem("posts", posts)
      this.setState({ posts })
    }
  }
  render() {
    return <div className="columns application">
      <div className="column is-one-quarter">
        <PostForm post={this.post} />
      </div>
      <div className="column tile posts">
        {this.state.posts.map((p) => <PostView deletePost={this.deletePost} {...p}></PostView>)}
      </div>
    </div>
  }
}
export default App;