import * as React from "react";
import * as Post from "./model/post"

type PostViewProps = {
  deletePost: (id: Post.Id) => void
} & Post;
class PostView extends React.Component<PostViewProps, never>{
  delete = () => {
    this.props.deletePost(this.props.id)
  }
  render() {
    return <div className="card post">
      <div className="card-content">
        <div className="content">
          <div className="title">{this.props.content}</div>
        </div>
      </div>
      <a className="delete-icon" onClick={this.delete}>
        <span className="icon">
          <i className="fa fa-remove"></i>
        </span>
      </a>
    </div>
  }
}
export default PostView;
export { Post }