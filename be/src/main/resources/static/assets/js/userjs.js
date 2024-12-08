$(document).on('ready', function () {
    if (window.localStorage.getItem('hs-builder-popover') === null) {
        $('#builderPopover').popover('show')
            .on('shown.bs.popover', function () {
                $('.popover').last().addClass('popover-dark')
            });

        $(document).on('click', '#closeBuilderPopover', function () {
            window.localStorage.setItem('hs-builder-popover', true);
            $('#builderPopover').popover('dispose');
        });
    } else {
        $('#builderPopover').on('show.bs.popover', function () {
            return false
        });
    }


});

const API_BASE_URL = 'http://localhost:8080/onboarding';
const AUTH_HEADER = { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` };

document.getElementById('editAvatarUploaderModal').addEventListener('change', function (event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const avatarImage = document.getElementById('editAvatarImgModal');
            avatarImage.src = e.target.result;  // Thay đổi src của hình ảnh
        };
        reader.readAsDataURL(file);
    }
});



// Hàm lấy dữ liệu người dùng và cập nhật các input trong modal
// async function fetchOneUseData(userId) {
//     const token = localStorage.getItem('authToken');
//     try {
//         const response = await fetch(`http://localhost:8080/onboarding/users/${userId}`, {
//             method: 'GET',
//             headers: {
//                 'Authorization': `Bearer ${token}`  // Thêm token vào header của yêu cầu
//             }
//         });
//         const data = await response.json();
//         console.log(data.data.fullname);



//         // Cập nhật các ô input trong modal với dữ liệu người dùng
//         // document.getElementById('editFullnameModal').value = data.data.fullname || '';  // Điền fullname vào input
//         document.getElementById('editPhoneModal').value = data.data.phoneNumber || '';  // Điền phone number vào input
//         document.getElementById('editBioModalLabel').value = data.data.bio || '';  // Điền email vào input



//     } catch (error) {
//         console.error('Có lỗi xảy ra khi tải dữ liệu người dùng:', error);
//     }
// }

// document.querySelectorAll('#edit').forEach(button => {
//     button.addEventListener('click', function () {
//         const userId = this.getAttribute('data-id');
//         const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
//         modal.show();  // Hiển thị modal khi nhấn nút


//         fetchOneUseData(userId);

//         // Lấy và cập nhật dữ liệu từ API (nếu cần)
//         const updateProfile = async (id) => {
//             try {
//                 // Tạo DTO từ form
//                 const token = localStorage.getItem('authToken');
//                 const phoneNumber = document.getElementById('editPhoneModal')?.value?.trim();
//                 const birthDate = document.getElementById('editBirthDayModalLabel')?.value?.trim();
//                 const bio = document.getElementById('editBioModalLabel')?.value?.trim();
//                 const avatarFile = document.getElementById('editAvatarUploaderModal')?.files[0];
//                 if (!phoneNumber || !birthDate || !bio) {
//                     throw new Error("Please fill in all fields.");
//                 }

//                 var dateParts = birthDate.split("-");
//                 var formattedDate = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];

//                 const updateDTO = {
//                     phoneNumber: phoneNumber,
//                     dob: formattedDate,
//                     bio: bio
//                 };

//                 const linkAPI = `http://localhost:8080/onboarding/change-profile/${id}`;
//                 const formData = new FormData();

//                 formData.append("updateDTO", new Blob([JSON.stringify(updateDTO)], { type: "application/json" }));

//                 // Thêm avatar nếu có
//                 if (avatarFile) {
//                     formData.append("imageFile", avatarFile);
//                 }

//                 // Gửi yêu cầu POST
//                 const response = await fetch(linkAPI, {
//                     method: "POST",
//                     body: formData,
//                     headers: {
//                         'Authorization': `Bearer ${token}`  // Thêm token vào header của yêu cầu
//                     }
//                 });
//                 const data = await response.json();

//                 console.log(data);


//                 if (data.status !== false) {
//                     modal.hide(); // Ẩn modal khi cập nhật thành công
//                     alert("Cập nhật thành công!");


//                 } else {
//                     throw new Error("Lỗi :" + data.error.message);
//                 }
//             } catch (error) {
//                 console.error("Error:", error);
//                 alert(error.message || "Đã có lỗi xảy ra.");
//             }
//         };
//         // Xử lý khi nhấn "Save Changes"
//         document.getElementById("submit").addEventListener("click", (e) => {
//             e.preventDefault();
//             if (userId) {
//                 updateProfile(userId);  // Gọi hàm cập nhật khi nhấn lưu
//             } else {
//                 alert("Không có ID người dùng!");
//             }
//         });
//     });
// });


function checkStatus(userId) {
    const openButton = document.getElementById('openAccount');
    const lockButton = document.getElementById('confirmLock');

    return fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(res => res.json())
        .then(data => {
            const userAccount = data.data.isLocked; // Lấy trạng thái tài khoản
            if (userAccount === "LOOK") {
                openButton.disabled = false;
                lockButton.disabled = true;
            } else {
                openButton.disabled = true;
                lockButton.disabled = false;
            }
            return userAccount; // Trả về trạng thái tài khoản
        })
        .catch(error => {
            console.error("Có lỗi xảy ra khi lấy thông tin người dùng:", error);
            alert("Có lỗi xảy ra khi lấy thông tin người dùng.");
            throw error; // Quăng lỗi để xử lý bên ngoài
        });
}

document.querySelectorAll('.btn-outline-success').forEach(button => {
    button.addEventListener('click', function () {
        const userId = this.getAttribute('data-id');
        const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
        modal.show();

        const openButton = document.getElementById('openAccount');
        const lockButton = document.getElementById('confirmLock');

        // Xóa các sự kiện cũ trước khi gắn sự kiện mới
        const newOpenButton = replaceButtonWithClone(openButton);
        const newLockButton = replaceButtonWithClone(lockButton);

        // Kiểm tra trạng thái tài khoản
        checkStatus(userId)
            .then(userAccount => {
                if (userAccount === "LOOK") {
                    newOpenButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL}/locked-account/${userId}/isLocked?isLocked=OPEN`, {
                            method: "PUT",
                            headers: AUTH_HEADER
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data) {
                                    modal.hide();
                                    alert("OPEN Succesfully")
                                    location.reload();
                                } else {
                                    alert("Có lỗi xảy ra khi mở khóa tài khoản.");
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                } else {
                    newLockButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL}/locked-account/${userId}/isLocked?isLocked=LOOK`, {
                            method: "PUT",
                            headers: AUTH_HEADER
                        })
                            .then(response => response.json())
                            .then(data => {
                                if (data) {
                                    modal.hide();
                                    alert("LOCK Succesfully")
                                    location.reload();
                                } else {
                                    alert("Có lỗi xảy ra khi khóa tài khoản.");
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                }
            })
            .catch(error => {
                console.error("Không thể kiểm tra trạng thái tài khoản:", error);
            });
    });
});

function replaceButtonWithClone(button) {
    const newButton = button.cloneNode(true);
    button.parentNode.replaceChild(newButton, button);
    return newButton;
}



document.getElementById("addUser").addEventListener("click", function () {
    const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
    modal.show();
});

function loadUserDetails(userId) {
    fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                const user = data.data;
                console.log(user);
                console.log(user.fullname);

                const avatarUrl = user.avatarUrl || '';
                if (avatarUrl) {
                    document.getElementById('userAvatarImgModal').src = avatarUrl;
                    document.getElementById('userAvatarImgModal').style.display = 'block';
                    document.getElementById('userAvatarTextModal').style.display = 'none';
                } else {
                    const firstLetter = user.fullname ? user.fullname.charAt(0).toUpperCase() : 'N'; // Lấy chữ cái đầu tiên của fullname
                    document.getElementById('avatarInitial').innerText = firstLetter;
                    document.getElementById('userAvatarTextModal').style.display = 'flex';
                    document.getElementById('userAvatarImgModal').style.display = 'none';
                }
                document.getElementById('managerFullNameDetails').innerText = user.fullname
                document.getElementById('managerRolesDetails').innerText = user.roles[0]
                const date = new Date(user.create_at);
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0'); // Tháng bắt đầu từ 0 nên cần +1
                document.getElementById("managerCreateTimeDetails").innerText = `Joined ${year} -${month}`;

                document.getElementById('userFullNamePro').innerText = 'Full Name: ' + (user.fullname || 'Not Available');
                document.getElementById('userPosition').innerText = 'Role: ' + (user.roles[0] || 'Not Available');
                document.getElementById('userBio').innerText = 'Bio: ' + (user.bio || 'Not Available');
                document.getElementById('userDOB').innerText = 'Date of Birth: ' + (user.dob || 'Not Available');

                // Hiển thị modal
                const modal = new bootstrap.Modal(document.getElementById('userDetailModal'));
                modal.show();

                joinedGroup(userId)
                userCreatedGroups(userId)


            } else {
                console.error('Lỗi khi tải thông tin người dùng');
            }
        })
        .catch(error => {
            console.error('Lỗi mạng:', error);
        });
}

document.getElementById("logoutLink").addEventListener("click", function () {
    window.location.replace("/web-server/login");
});


document.querySelectorAll('.nav-link').forEach(tab => {
    tab.addEventListener('click', function () {
        // Ẩn phần tử profile khi chuyển sang tab khác
        document.querySelector('#profile').style.display = 'none';

        // Hiển thị lại profile khi tab Profile được chọn
        if (this.id === 'profile-tab') {
            document.querySelector('#profile').style.display = 'block';
        }
    });
});

// Hàm lấy tên nhóm theo groupId
function getGroupDetails(groupId) {
    const url = `http://localhost:8080/api/groups/${groupId}`;

    return fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER,
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch group details for ID ${groupId}, Status: ${response.status}`);
            }
            return response.json();  // Nếu phản hồi hợp lệ, chuyển đổi thành JSON
        })
        .then(data => {
            if (data.status) {
                return data.data.group_name;  // Giả sử dữ liệu trả về có groupName
            } else {
                console.error("Failed to fetch group details:", data.message);
                return null;
            }
        })
        .catch(error => {
            console.error("Error:", error);
            return null;
        });
}

function joinedGroup(userId) {
    // Tạo URL từ groupId
    const url = `http://localhost:8080/api/member/group-join-id/${userId}`;

    // Gửi yêu cầu GET tới API để lấy danh sách groupId mà người dùng tham gia
    fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER, // Thêm header nếu cần
    })
        .then(response => {
            // Kiểm tra mã trạng thái HTTP
            if (!response.ok) {
                throw new Error(`Failed to fetch user group IDs, Status: ${response.status}`);
            }
            return response.json(); // Chuyển phản hồi thành JSON
        })
        .then(data => {
            const groupIds = data.data; // Mảng các groupId
            const groupList = document.getElementById('joined-groups-list');

            // Xóa các phần tử cũ trước khi thêm mới
            groupList.innerHTML = '';

            if (!groupIds || groupIds.length === 0) {
                // Nếu không có groupId, hiển thị thông báo
                const noGroupItem = document.createElement('li');
                noGroupItem.textContent = 'User has not joined any groups';
                groupList.appendChild(noGroupItem);
                return;
            }

            // Lấy tên các nhóm dựa trên groupId
            const groupNamesPromises = groupIds.map(groupId => getGroupDetails(groupId));

            Promise.all(groupNamesPromises)
                .then(groupNames => {
                    // Thêm các tên nhóm vào danh sách
                    groupNames.forEach(groupName => {
                        if (groupName) {
                            const listItem = document.createElement('li');
                            listItem.textContent = `Group: ${groupName}`;
                            groupList.appendChild(listItem);
                        }
                    });
                })
                .catch(error => {
                    console.error('Error fetching group names:', error);
                });
        })
        .catch(error => {
            console.error('Error joining group:', error);
        });
}


function userCreatedGroups(userId) {
    // URL API để lấy danh sách groupId mà người dùng đã tạo
    const url = `http://localhost:8080/api/member/user-create-id/${userId}`;

    fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER, // Thêm header xác thực nếu cần
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch created groups, Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng groupId
            const groupDetailsPromises = groupIds.map(groupId => getGroupDetails(groupId)); // Gọi API chi tiết group

            Promise.all(groupDetailsPromises)
                .then(groupNames => {
                    const groupList = document.getElementById('created-groups-list');
                    groupList.innerHTML = ''; // Xóa nội dung cũ nếu có

                    if (groupNames.length === 0) {
                        groupList.innerHTML = `<div>Người dùng chưa tạo nhóm nào</div>`;
                    } else {
                        groupNames.forEach(groupName => {
                            if (groupName) {
                                const groupDiv = document.createElement('div');
                                groupDiv.textContent = `Group: ${groupName}`;
                                groupDiv.className = 'group-item'; // Thêm class nếu cần để CSS
                                groupList.appendChild(groupDiv);
                            }
                        });
                    }
                })
                .catch(error => {
                    console.error('Error fetching group details:', error);
                });
        })
        .catch(error => {
            console.error('Error fetching user-created groups:', error);
        });
}


    
});


