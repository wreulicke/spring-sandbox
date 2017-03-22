import * as React from "react";
import Post from "./model/post"

type PostViewProps = {
  deletePost: (content: string) => void
} & Post;
class PostView extends React.Component<PostViewProps, never>{
  delete = () => {
    this.props.deletePost(this.props.content)
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