import x from "../../main/front/test"

jest.unmock("../../main/front/test")

describe("test", () => {
  it("no error", function () {
    x()
  })
})