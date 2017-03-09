
export default function () {
  console.log("test")
  if (typeof window == "object") {
    console.log("not reached")
  }
}