const url1 = `ws://${location.host}${location.pathname}echo`;
const url2 = `ws://${location.host}${location.pathname}test`;
test(url1);
test(url2);
function test(url) {
  const ws= new WebSocket(url);
  const bind=(f, v)=> f.bind(null, v);
  const log=console.log.bind(console);

  ws.onclose=bind(log, "close");
  ws.onerror=bind(log, "error");
  ws.onmessage=bind(log, "message");
  ws.onopen=()=> ws.send("send text");
}

