import * as React from "react";

type PostFormProps = {
  post: (text: string) => void
}
class PostForm extends React.Component<PostFormProps, never>{
  onKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.ctrlKey && e.key == "Enter") {
      if (e.currentTarget.value !== "") {
        this.props.post(e.currentTarget.value)
        e.currentTarget.value = ""
      }
    }
  }
  render() {
    return <div className="post-form contaienr">
      <header className="post-form-header">
        <h1 className="title">New Tweets</h1>
      </header>
      <input className="input"
        type="text"
        placeholder="What's happening"
        required={true} onKeyDown={this.onKeyDown} />
    </div >
  }
}
export default PostForm;