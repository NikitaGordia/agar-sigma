
//Settings
const INT_SIZE = 4
const CANVAS_TRANSLATE = 0.5
const GRID_SIZE = 20
const GRID_WIDTH_RATIO = 0.2
const CIRCLE_SCALE = 0.1
const WEB_SOCKET_URL = 'ws://localhost:8081/sigma/agar'

//Colors
const BUBBLE_COLOR = '#dec513'
const SNACK_COLOR = '#50de13'
const STROKE_COLOR = '#d1d7db'

function nextInt(x, sh) {
    return (x[sh] & 0xFF) << 24 | (x[sh + 1] & 0xFF) << 16 | (x[sh + 2] & 0xFF) << 8 | (x[sh + 3] & 0xFF)
}

//Socket
let socket = new WebSocket(WEB_SOCKET_URL)
socket.binaryType = "arraybuffer"
socket.onopen = function () {
    document.addEventListener('mousemove', function (event) {
        let rect = cs.getBoundingClientRect()
        socket.send(JSON.stringify({
            x: (event.clientX - rect.left) - rect.width * CANVAS_TRANSLATE,
            y: (event.clientY - rect.top) - rect.height * CANVAS_TRANSLATE
        }))
    }, false)
}

//Canvas
let cs
window.onload = function () {
    cs = document.getElementById('canvas')
}
socket.onmessage = function (event) {
    const ctx = cs.getContext('2d')
    const width = window.innerWidth
    const height = window.innerHeight
    ctx.canvas.width = width
    ctx.canvas.height = height
    ctx.translate(
        width * CANVAS_TRANSLATE,
        height * CANVAS_TRANSLATE
    )

    let shift = 0
    const obj = pako.inflate(event.data)
    const server_width = nextInt(obj, 2 * INT_SIZE)
    const server_height = nextInt(obj, 3 * INT_SIZE)
    const x = nextInt(obj, 0)
    const y = nextInt(obj, INT_SIZE)
    let screen_k
    if (width > height) {
        screen_k = width / server_width
    } else {
        screen_k = height / server_height
    }

    render_background(x, y, width, height, ctx, screen_k)

    shift += 4 * INT_SIZE
    for (; shift < obj.length;) {
        switch (nextInt(obj, shift) >> 1) {
            case 1:
                ctx.fillStyle = BUBBLE_COLOR
                break
            case 0:
                ctx.fillStyle = SNACK_COLOR
                break
        }
        let circle_r = nextInt(obj, shift + INT_SIZE) * screen_k
        let circle_x = (nextInt(obj, shift + 2 * INT_SIZE) - x) * screen_k
        let circle_y = (nextInt(obj, shift + 3 * INT_SIZE) - y) * screen_k
        shift += 4 * INT_SIZE

        ctx.beginPath()
        ctx.arc(
            circle_x,
            circle_y,
            circle_r * (1 - CIRCLE_SCALE * CANVAS_TRANSLATE),
            0,
            Math.PI * 2,
            true
        )
        ctx.closePath()
        ctx.fill()
    }
}

function render_background(x, y, width, height, ctx, screen_k) {
    const n = width / GRID_SIZE
    const m = height / GRID_SIZE
    let vertical_inf = width * CANVAS_TRANSLATE
    let horizontal_inf = height * CANVAS_TRANSLATE
    let x_screen = (x % GRID_SIZE) + vertical_inf
    let y_screen = (y % GRID_SIZE) + horizontal_inf
    vertical_inf *= screen_k
    horizontal_inf *= screen_k
    ctx.lineWidth = GRID_WIDTH_RATIO * screen_k
    ctx.strokeStyle = STROKE_COLOR

    ctx.beginPath()
    for (let i = 0; i < n; i++) {
        let j = (i * GRID_SIZE - x_screen) * screen_k
        ctx.moveTo(j, -horizontal_inf)
        ctx.lineTo(j, horizontal_inf)
    }
    for (let i = 0; i < m; i++) {
        let j = (i * GRID_SIZE - y_screen) * screen_k
        ctx.moveTo(-vertical_inf, j)
        ctx.lineTo(vertical_inf, j)
    }
    ctx.stroke()
}