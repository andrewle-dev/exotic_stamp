# Hướng dẫn sử dụng Admin Dashboard Exotic Stamp

## 1. Giới thiệu

Admin Dashboard Exotic Stamp là công cụ dành cho người vận hành, nội dung và quản trị để nhập, kiểm tra và quản lý dữ liệu phục vụ trải nghiệm trên ứng dụng mobile.

### Mục đích của Admin Dashboard

Dashboard giúp doanh nghiệp:
- quản lý thông tin ga, chiến dịch và con dấu sưu tập
- kiểm soát thời điểm và nội dung hiển thị cho người dùng
- đảm bảo dữ liệu nhập vào đồng bộ, chính xác và ổn định trên mobile

### Ai nên sử dụng

Tài liệu này phù hợp cho:
- CEO hoặc người quản lý vận hành
- nhân viên admin nội bộ
- nhân viên marketing / content admin
- người phụ trách dữ liệu và nội dung trên app

### Nguyên tắc chung khi nhập dữ liệu

Khi làm việc trong dashboard, hãy luôn nhớ 5 nguyên tắc sau:
1. Nhập dữ liệu đúng mục đích nghiệp vụ.
2. Kiểm tra lại trước khi lưu hoặc kích hoạt.
3. Không nhập dữ liệu tùy tiện chỉ vì “đã xong”.
4. Đảm bảo thông tin giữa ga, chiến dịch, con dấu và phần thưởng luôn thống nhất.
5. Nếu không chắc chắn, hãy hỏi người phụ trách trước khi lưu.

---

## 2. Nguyên tắc vận hành quan trọng

### Không nhập dữ liệu tùy tiện

Mỗi thông tin trong admin đều có thể ảnh hưởng trực tiếp đến trải nghiệm người dùng trên mobile. Vì vậy:
- không nên nhập tên, mã, hình ảnh hoặc trạng thái một cách tùy ý
- nên dùng dữ liệu chuẩn và nhất quán

### Luôn kiểm tra lại trước khi lưu

Trước khi bấm lưu hoặc kích hoạt, hãy kiểm tra:
- tên và mã có đúng chưa
- thông tin có bị trùng lặp không
- ảnh có rõ nét, đúng tỷ lệ và đúng mục đích không
- campaign, station và stamp design có liên kết đúng không

### Dữ liệu trong admin ảnh hưởng trực tiếp đến mobile app

Thông tin được nhập trong admin sẽ quyết định:
- ga nào xuất hiện trên app
- chiến dịch nào đang chạy
- con dấu nào được hiển thị
- phần thưởng nào người dùng có thể nhận

Nếu dữ liệu sai, người dùng có thể thấy nội dung sai, không thấy con dấu, hoặc không nhận được phần thưởng đúng.

### Nên nhập theo đúng thứ tự nghiệp vụ

Không nên làm ngược thứ tự. Việc nhập theo đúng trình tự giúp hệ thống ít lỗi hơn và dễ kiểm soát.

### Ảnh, campaign, station, stamp design phải đồng bộ

Một chiến dịch chỉ hoạt động tốt khi các yếu tố liên quan đều sẵn sàng:
- ga đã có hồ sơ đúng
- campaign đã được tạo và gán đúng ga
- stamp design đã được tạo cho ga trong chiến dịch đó
- ảnh và trạng thái đã chuẩn

### Không tự ý thay đổi các thông tin kỹ thuật nếu không hiểu rõ

Nếu thấy các mục như mã, trạng thái hệ thống, quyền truy cập hoặc môi trường hệ thống, hãy chỉ chỉnh khi thật sự cần và có người phụ trách. Không tự ý đổi để “thử”.

---

## 3. Thứ tự nhập liệu khuyến nghị

Để vận hành ổn định, nên nhập theo thứ tự sau:

1. Metro Lines
2. Stations
3. Campaigns
4. Stamp Designs
5. Partners
6. Milestones
7. Rewards & Vouchers
8. Analytics / RBAC chỉ dùng khi cần

### Vì sao thứ tự này quan trọng

- Metro Lines là nền tảng để sắp xếp hệ thống ga.
- Stations cần được tạo trước để có “địa điểm” cho chiến dịch.
- Campaigns cần được tạo sau khi đã có ga phù hợp.
- Stamp Designs phải gắn với campaign và station đúng để mobile hiển thị đúng nội dung.
- Partners, milestones và rewards nên được tạo sau khi chiến dịch đã có khung cơ bản.

Nếu làm ngược thứ tự, rất dễ gặp tình trạng:
- campaign tồn tại nhưng chưa có ga liên quan
- stamp không hiện do thiếu mapping
- phần thưởng không liên kết đúng chiến dịch

---

## 4. Hướng dẫn từng module

## A. Dashboard

### Dashboard dùng để làm gì

Dashboard là màn hình tổng quan giúp người quản trị xem tình hình vận hành chung.

### Khi nào cần dùng

- khi cần xem tổng quan nhanh
- khi kiểm tra hoạt động của các module chính
- khi muốn có nhìn tổng thể trước khi chỉnh sửa dữ liệu

### Điều cần lưu ý

- Dashboard không phải nơi để nhập dữ liệu chính
- nên dùng để theo dõi tình trạng và phát hiện vấn đề sớm

### Lỗi thường gặp

- chỉ nhìn dashboard nhưng không kiểm tra chi tiết ở module liên quan
- bỏ qua các dữ liệu chưa được kích hoạt hoặc chưa đồng bộ

---

## B. Metro Lines

### Metro Lines dùng để làm gì

Metro Lines là nền tảng để tạo ra các tuyến/line cho hệ thống ga. Đây là bước đầu tiên trong cấu trúc dữ liệu vận hành.

### Khi nào cần dùng

- khi cần tạo tuyến mới cho hệ thống
- khi cần sắp xếp danh sách ga theo tuyến

### Các trường quan trọng

- tên tuyến
- mã tuyến (nếu có)
- thứ tự hiển thị

### Lưu ý quan trọng

- không nhập trùng mã hoặc tên tuyến
- không nên tạo line trùng lặp chỉ vì muốn “đặt tên khác”
- thứ tự tuyến cần nhất quán để danh sách ga hiển thị đúng

### Lỗi thường gặp

- nhập trùng tuyến
- bỏ sót thứ tự tuyến
- tạo tuyến không dùng đến

### Tác động đến mobile

Nếu tuyến sai hoặc trùng, danh sách ga trên mobile có thể hiển thị sai hoặc khó hiểu cho người dùng.

---

## C. Stations

### Stations dùng để làm gì

Stations là hồ sơ ga vật lý trong hệ thống. Đây là dữ liệu nền cho các ga mà người dùng sẽ thấy trên ứng dụng.

### Khi nào cần dùng

- khi thêm ga mới
- khi chỉnh sửa thông tin ga hiện có
- khi cần cập nhật địa chỉ, GPS, trạng thái hoặc hình ảnh ga

### Các trường quan trọng

- tên ga
- mã ga / code
- tuyến line
- trạng thái hoạt động
- địa chỉ
- GPS / vị trí
- scan key
- hình đại diện ga

### Lưu ý quan trọng

- station là hồ sơ ga vật lý, không phải artwork con dấu
- nếu nhập sai GPS hoặc trạng thái, mobile có thể hiển thị sai vị trí hoặc không scan đúng
- scan key phải chính xác để trải nghiệm quét/nhận thông tin hoạt động tốt
- hình ga dùng để hiển thị hồ sơ ga, không phải hình con dấu

### Khi nào cần sửa station

- ga mới mở
- ga đổi tên hoặc đổi tuyến
- ga tạm đóng hoặc mở lại
- ga thay đổi vị trí hoặc GPS

### Lỗi thường gặp

- nhập sai line
- thiếu GPS hoặc GPS sai
- nhập trùng code hoặc tên
- upload hình ảnh không phù hợp

### Tác động đến mobile

- station sai → người dùng thấy tên ga sai, vị trí sai, hoặc không thể tương tác đúng
- nếu scan key sai, trải nghiệm quét có thể bị lỗi

---

## D. Campaigns

### Campaigns dùng để làm gì

Campaigns là các chiến dịch sưu tập hoặc đợt hoạt động mà người dùng sẽ tham gia.

### Khi nào cần dùng

- khi bắt đầu một chương trình mới
- khi mở một đợt sưu tập riêng biệt
- khi muốn kích hoạt một tập hợp ga và con dấu cho một thời gian nhất định

### Các trường quan trọng

- tên chiến dịch
- thời gian bắt đầu và kết thúc
- trạng thái hoạt động
- ga được gán trong chiến dịch

### Lưu ý quan trọng

- một line có thể có nhiều campaign
- campaign quyết định thời gian và đợt hiển thị stamp
- campaign active nhưng chưa gán station sẽ gây rối trong vận hành

### Lỗi thường gặp

- tạo campaign nhưng chưa gán station
- kích hoạt campaign mà chưa chuẩn bị dữ liệu liên quan
- gán sai ga cho campaign

### Tác động đến mobile

Nếu campaign sai, người dùng có thể không thấy chiến dịch đúng thời điểm, hoặc không thấy dữ liệu sưu tập phù hợp.

---

## E. Stamp Designs

### Stamp Designs dùng để làm gì

Stamp design là hình con dấu sưu tập được hiển thị trong một chiến dịch cụ thể tại một station nhất định.

### Khi nào cần dùng

- khi cần tạo con dấu cho một ga trong một chiến dịch
- khi có chiến dịch mới cần hình con dấu riêng
- khi đổi artwork cho một chiến dịch cụ thể

### Các trường quan trọng

- campaign
- station
- tên/nhãn con dấu
- trạng thái
- ảnh con dấu
- độ hiếm / rarity (nếu có)

### Lưu ý quan trọng

- stamp design không phải là hình đại diện ga
- cùng một station có thể có nhiều stamp design ở các campaign khác nhau
- khi nhập sai campaign hoặc station, mobile sẽ hiển thị con dấu sai hoặc không có con dấu

### Lỗi thường gặp

- tạo stamp design cho campaign sai
- gắn sai station
- dùng ảnh không đúng chuẩn
- nhập duplicate hoặc trùng nội dung không cần thiết

### Tác động đến mobile

Nếu stamp design sai, người dùng có thể thấy hình con dấu sai, không thấy con dấu, hoặc hiểu nhầm về chiến dịch.

---

## F. Partners

### Partners dùng để làm gì

Partners dùng để quản lý thương hiệu, đối tác hoặc đơn vị liên quan trong chương trình.

### Khi nào cần dùng

- khi có đối tác mới tham gia chiến dịch
- khi cần hiển thị logo hoặc banner đối tác

### Các trường quan trọng

- tên đối tác
- trạng thái hoạt động
- logo
- banner

### Lưu ý quan trọng

- logo và banner là hai thứ khác nhau
- logo thường dùng cho thương hiệu nhỏ, banner dùng cho màn hình nổi bật
- ảnh nên đúng chuẩn tỷ lệ và chất lượng

### Lỗi thường gặp

- dùng banner làm logo hoặc ngược lại
- upload ảnh quá nhỏ hoặc mờ
- không deactivate đúng khi đối tác kết thúc hợp tác

### Tác động đến mobile

Nếu hình ảnh đối tác sai, giao diện trên app có thể trông không chuyên nghiệp hoặc gây nhầm lẫn.

---

## G. Milestones

### Milestones dùng để làm gì

Milestones là các mốc nhận thưởng theo số lượng stamp đã thu thập.

### Khi nào cần dùng

- khi thiết kế phần thưởng theo tiến trình người dùng tích lũy con dấu
- khi muốn tạo các cấp độ nhận thưởng khác nhau

### Các trường quan trọng

- campaign liên quan
- số stamp cần đạt
- tiêu đề mốc
- loại phần thưởng

### Lưu ý quan trọng

- milestone phải gắn với campaign đúng
- số stamp cần đạt phải hợp lý và rõ ràng
- tiêu đề và loại thưởng phải dễ hiểu cho người dùng

### Lỗi thường gặp

- tạo milestone không gắn campaign
- nhập số stamp không hợp lý
- mô tả thưởng chưa rõ

### Tác động đến mobile

Milestone sai có thể khiến người dùng nhận thưởng không đúng hoặc không hiểu được tiến trình của mình.

---

## H. Rewards & Vouchers

### Rewards & Vouchers dùng để làm gì

Đây là nơi quản lý phần thưởng và voucher dành cho người dùng.

### Khi nào cần dùng

- khi chuẩn bị phần thưởng cho chiến dịch
- khi phát hành voucher cho người tham gia

### Các trường quan trọng

- tên phần thưởng
- loại reward
- mã voucher
- trạng thái hoạt động
- thời gian hiệu lực (nếu có)

### Lưu ý quan trọng

- voucher là mã cụ thể, không nên nhập trùng mã
- không nên disable nhầm voucher đang dùng
- mã cần rõ ràng và dễ kiểm tra

### Lỗi thường gặp

- nhập trùng mã voucher
- vô tình tắt voucher đang hoạt động
- nhập thông tin reward không phù hợp với campaign

### Tác động đến mobile

Nếu reward/voucher sai, người dùng có thể không nhận thưởng đúng hoặc redeem lỗi khi dùng app.

---

## I. Analytics

### Analytics dùng để làm gì

Analytics dùng để theo dõi tình hình hoạt động và hiệu quả của các dữ liệu đã nhập.

### Khi nào cần dùng

- khi cần kiểm tra tiến độ chiến dịch
- khi muốn xem dữ liệu hoạt động có vấn đề hay không

### Lưu ý quan trọng

- Analytics không phải nơi để nhập dữ liệu chính
- nên dùng như công cụ kiểm tra và giám sát

---

## J. RBAC

### RBAC dùng để làm gì

RBAC là khu vực quản lý quyền truy cập trong hệ thống.

### Khi nào cần dùng

- khi cần phân quyền cho người dùng admin
- khi cần xác định ai có thể thao tác dữ liệu nào

### Lưu ý quan trọng

- chỉ dành cho người được phân quyền
- không tự ý sửa role hoặc permission nếu không hiểu đầy đủ
- cần tránh tình trạng khóa chính mình khỏi quyền thao tác cần thiết

### Lỗi thường gặp

- cấp sai quyền
- sửa quyền mà chưa xác nhận tác động

---

## K. Settings

### Settings dùng để làm gì

Settings hiện chủ yếu dùng cho các thiết lập tài khoản và thông tin hệ thống liên quan đến phiên làm việc.

### Khi nào cần dùng

- khi kiểm tra thông tin tài khoản đang đăng nhập
- khi xem thông tin môi trường hệ thống
- khi cần đăng xuất

### Lưu ý quan trọng

- Settings không phải nơi để nhập dữ liệu nghiệp vụ chính như campaign, station hay stamp design
- không dùng Settings để thay đổi dữ liệu nội dung chính của hệ thống

---

## 5. Quy tắc ảnh và tài sản hình ảnh

Ảnh là một phần rất quan trọng trong hệ thống. Nếu ảnh sai, trải nghiệm mobile sẽ bị ảnh hưởng ngay.

### 1) Stamp design

- ảnh nên vuông theo tỷ lệ 1:1
- khuyến nghị 2048x2048 hoặc 2560x2560
- tối thiểu 1024x1024
- dùng cho màn hình sưu tập / stamp book

### 2) Partner logo

- ảnh/logo gần vuông, nên dùng tỷ lệ 1:1
- khuyến nghị 1024x1024
- tối thiểu 512x512
- ưu tiên PNG nền trong suốt nếu có

### 3) Partner banner

- ảnh ngang theo tỷ lệ 16:9
- khuyến nghị 1920x1080
- tối thiểu 1280x720

### 4) Station media

- là hình ảnh dùng cho hồ sơ ga và hiển thị ga trên app
- cần rõ ràng, đúng định dạng và phù hợp với màn hình ga
- không nên dùng ảnh con dấu thay cho ảnh ga

### Nguyên tắc chung về ảnh

- không dùng ảnh mờ, quá nhỏ hoặc méo tỷ lệ
- nên xem trước khi lưu
- nếu ảnh sai tỉ lệ, mobile có thể hiển thị không đẹp hoặc bị crop sai

---

## 6. Phân biệt rõ Station và Stamp Design

Đây là điểm rất quan trọng và thường bị hiểu nhầm.

### Station là gì

Station là hồ sơ ga vật lý. Nó mô tả ga nào đó trong hệ thống: tên ga, địa chỉ, tuyến, vị trí, trạng thái và hình ảnh ga.

### Stamp Design là gì

Stamp design là artwork con dấu của ga trong một campaign cụ thể.

### Điểm khác biệt quan trọng

- Một ga có thể có nhiều stamp design qua nhiều campaign khác nhau.
- Station không phải là stamp design.
- Hình ảnh ga và hình ảnh con dấu là hai thứ khác nhau.

### Vì sao cần phân biệt

Nếu hiểu nhầm, dữ liệu trên mobile sẽ bị sai: người dùng có thể thấy ga đúng nhưng con dấu sai, hoặc ngược lại.

---

## 7. Các lỗi thường gặp cần tránh

| Lỗi | Hậu quả | Cách tránh |
|---|---|---|
| Tạo campaign nhưng chưa gán station | Chiến dịch không hoạt động đúng logic | Kiểm tra lại trước khi kích hoạt |
| Tạo station nhưng thiếu GPS hoặc sai line | Ga hiển thị sai hoặc không hoạt động đúng | Điền đầy đủ thông tin trước khi lưu |
| Upload ảnh sai tỷ lệ | Hình hiển thị méo, bị crop sai hoặc không đẹp | Dùng ảnh đúng chuẩn kích thước |
| Nhập dữ liệu trùng code / tên / mã voucher | Dữ liệu bị lẫn lộn và khó kiểm soát | Kiểm tra trước khi lưu |
| Tạo stamp design sai campaign hoặc sai station | Mobile hiển thị con dấu sai | Chọn campaign và station thật kỹ |
| Xóa hoặc deactivate nhầm item đang dùng | Nội dung có thể biến mất hoặc ngừng hoạt động | Chỉ thao tác khi chắc chắn |
| Sửa role / permission không đúng | Có thể ảnh hưởng đến quyền vận hành | Chỉ thực hiện bởi người được phép |
| Chỉ xem dữ liệu trong admin mà không kiểm tra trên mobile | Một số lỗi chỉ thấy khi dùng app thật | Luôn kiểm tra lại trên trải nghiệm người dùng |

---

## 8. Tác động của dữ liệu admin đến mobile app

Thông tin nhập trong admin ảnh hưởng trực tiếp đến cách app hoạt động.

### Nếu station sai
- người dùng có thể thấy tên ga sai
- vị trí hoặc trạng thái ga có thể không đúng
- trải nghiệm scan có thể bị ảnh hưởng

### Nếu campaign sai
- chiến dịch có thể không hiển thị đúng thời điểm
- người dùng sẽ không thấy đợt sưu tập phù hợp

### Nếu stamp design sai
- hình con dấu có thể sai, thiếu hoặc không hiện

### Nếu milestone hoặc reward sai
- người dùng có thể không nhận thưởng đúng
- tiến trình tích lũy có thể bị hiểu sai

### Nếu voucher sai
- người dùng có thể redeem lỗi hoặc không nhận được phần thưởng như mong đợi

---

## 9. Quy trình làm việc thực tế

## 1) Quy trình tạo chiến dịch mới hoàn chỉnh

1. Tạo campaign mới.
2. Gán các station phù hợp cho chiến dịch.
3. Tạo stamp design cho từng station trong chiến dịch đó.
4. Kiểm tra ảnh con dấu và hình ga.
5. Tạo milestone nếu cần.
6. Tạo reward hoặc voucher nếu có phần thưởng đi kèm.
7. Kiểm tra lại trước khi kích hoạt.

### Mục tiêu

Đảm bảo chiến dịch có đầy đủ các thành phần để người dùng nhìn thấy và tương tác đúng trên mobile.

---

## 2) Quy trình thêm ga mới

1. Tạo station mới.
2. Nhập tên ga, line, code và trạng thái.
3. Điền GPS và thông tin liên quan.
4. Upload ảnh ga phù hợp.
5. Kiểm tra station đã sẵn sàng chưa.
6. Nếu chiến dịch đang chạy, gán ga vào campaign phù hợp và tạo stamp design tương ứng.

### Mục tiêu

Đảm bảo ga mới có thể xuất hiện đúng và hoạt động ổn định trên app.

---

## 3) Quy trình thêm đối tác + banner + logo

1. Tạo partner mới.
2. Upload logo đúng chuẩn.
3. Upload banner đúng chuẩn.
4. Kiểm tra xem logo và banner có phù hợp với chiến dịch không.
5. Kích hoạt đối tác khi đã sẵn sàng.

### Mục tiêu

Đảm bảo đối tác hiển thị đúng và chuyên nghiệp trên app.

---

## 4) Quy trình import voucher an toàn

1. Kiểm tra danh sách voucher trước khi import.
2. Đảm bảo mã voucher chưa bị trùng.
3. Chọn đúng campaign hoặc reward liên quan.
4. Kiểm tra trạng thái hoạt động trước khi phát hành.
5. Sau khi import, kiểm tra lại một lần nữa.

### Mục tiêu

Tránh lỗi nhận thưởng hoặc nhầm mã voucher.

---

## 10. Checklist trước khi kết thúc thao tác

Trước khi kết thúc một thao tác quan trọng, hãy tự hỏi:
- Tôi đã kiểm tra lại thông tin chưa?
- Ảnh đã đúng chuẩn chưa?
- Campaign / station / stamp design có liên kết đúng chưa?
- Nếu có phần thưởng, voucher có đúng không?
- Nếu dữ liệu này xuất hiện trên app, người dùng sẽ hiểu đúng không?

Nếu câu trả lời là “chưa chắc”, hãy kiểm tra lại trước khi lưu hoặc kích hoạt.

---

## 11. Ghi chú về screenshot và minh họa

Nếu cần, có thể chèn ảnh minh họa cho từng màn hình trong tài liệu sau này, ví dụ:
- [Chèn ảnh minh họa màn hình Dashboard tại đây]
- [Chèn ảnh minh họa màn hình Stations tại đây]
- [Chèn ảnh minh họa màn hình Campaigns tại đây]
- [Chèn ảnh minh họa màn hình Stamp Designs tại đây]

Hiện tại, tài liệu này đã được viết theo hướng sử dụng thực tế cho người vận hành doanh nghiệp, không đề cập đến mã nguồn hay kiến trúc hệ thống.
