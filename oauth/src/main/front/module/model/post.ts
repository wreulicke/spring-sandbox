type Post = {
  id: number,
  content: string
};
declare var post: Post;
declare namespace Post {
  export type Id = typeof post.id
  export type Content = typeof post.content
}

export = Post