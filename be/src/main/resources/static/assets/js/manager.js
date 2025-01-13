const API_BASE_URL = 'http://localhost:8080/onboarding';
const AUTH_HEADER = { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` };
function loadUserDetails(userId) {
    fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                const user = data.data;
                const avatarUrl = user.avatarUrl || '';

                if (avatarUrl && avatarUrl !== "") {
                    // Nếu avatarUrl hợp lệ, hiển thị ảnh và ẩn phần chữ
                    document.getElementById('userAvatarImgModals').src = avatarUrl;
                    document.getElementById('userAvatarImgModals').style.display = 'block';
                    document.getElementById('userAvatarTextModals').style.display = 'none'; // Ẩn chữ
                } else {
                    // Nếu avatarUrl không có giá trị, hiển thị chữ cái đầu tiên của tên người dùng và ẩn ảnh
                    const firstLetter = user.fullname ? user.fullname.charAt(0).toUpperCase() : 'N';
                    document.getElementById('avatarInitials').innerText = firstLetter;
                    document.getElementById('userAvatarTextModals').style.display = 'flex'; // Hiển thị chữ
                    document.getElementById('userAvatarImgModals').style.display = 'none'; // Ẩn ảnh
                }



                document.getElementById('managerFullNameDetails').innerText = user.fullname;
                document.getElementById('managerRolesDetails').innerText = user.roles[0];
                const date = new Date(user.create_at);
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                document.getElementById("managerCreateTimeDetails").innerText = `Tham gia ${year} - ${month}`;

                document.getElementById('userFullNamePro').innerText = 'Họ tên: ' + (user.fullname || 'Không có thông tin');
                document.getElementById('userPosition').innerText = 'Chức vụ: ' + (user.roles[0] || 'Không có thông tin');
                document.getElementById('userBio').innerText = 'Giới thiệu: ' + (user.bio || 'Không có thông tin');
                document.getElementById('userDOB').innerText = 'Ngày sinh: ' + (user.dob || 'Không có thông tin');

                // Hiển thị modal
                const modal = new bootstrap.Modal(document.getElementById('userDetailModals'));
                modal.show();

                joinedGroup(userId);
                userCreatedGroups(userId);
                getFriendsList(userId);

            } else {
                console.error('Lỗi khi tải thông tin người dùng');
            }
        })
        .catch(error => {
            console.error('Lỗi mạng:', error);
        });
}
function getGroupDetails(groupId) {
    const url = `http://localhost:8080/api/groups/${groupId}`;

    return fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER,
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy chi tiết nhóm với ID ${groupId}, Mã trạng thái: ${response.status}`);
            }
            return response.json(); // Chuyển đổi response thành JSON
        })
        .then(data => {
            if (data.status && data.data) {
                return {
                    group_name: data.data.group_name || 'Không có',      // Tên nhóm
                    admin_name: data.data.admin_name || 'Không có',      // Tên admin
                    member_count: data.data.member_count || 0,           // Số lượng thành viên
                };
            } else {
                console.error(`Không thể lấy chi tiết nhóm: ${data.message}`);
                return null;
            }
        })
        .catch(error => {
            console.error("Lỗi khi lấy thông tin nhóm:", error);
            return null;
        });
}


function joinedGroup(userId) {
    const url = `http://localhost:8080/api/member/group-join-id/${userId}`;

    fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER, // Thêm header xác thực nếu cần
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh sách nhóm người dùng, Mã trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng các groupId
            const groupTableBody = document.getElementById('joined-groups-table-body');

            groupTableBody.innerHTML = ''; // Xóa các phần tử cũ trước khi thêm mới

            if (!groupIds || groupIds.length === 0) {
                const noGroupsRow = document.createElement('tr');
                noGroupsRow.innerHTML = '<td colspan="4" class="text-center">Người dùng chưa tham gia nhóm nào</td>';
                groupTableBody.appendChild(noGroupsRow);
                return;
            }

            // Lấy chi tiết các nhóm dựa trên groupId
            const groupDetailsPromises = groupIds.map(groupId => getGroupDetails(groupId));

            Promise.all(groupDetailsPromises)
                .then(groupDetails => {
                    groupDetails.forEach((group, index) => {
                        if (group) {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <th scope="row">${index + 1}</th>
                                <td>${group.group_name}</td> <!-- Tên nhóm -->
                                <td>${group.admin_name}</td> <!-- Tên admin -->
                                <td>${group.member_count}</td> <!-- Số lượng thành viên -->
                            `;
                            groupTableBody.appendChild(row);
                        }
                    });
                })
                .catch(error => {
                    console.error('Lỗi khi lấy chi tiết nhóm:', error);
                });
        })
        .catch(error => {
            console.error('Lỗi khi lấy danh sách nhóm người dùng:', error);
        });
}



function userCreatedGroups(userId) {
    const url = `http://localhost:8080/api/member/user-create-id/${userId}`;

    fetch(url, {
        method: 'GET',
        headers: AUTH_HEADER, // Thêm header xác thực nếu cần
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh sách nhóm đã tạo, Mã trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const groupIds = data.data; // Mảng groupId
            const groupDetailsPromises = groupIds.map(groupId => getGroupDetails(groupId)); // Gọi API chi tiết group

            Promise.all(groupDetailsPromises)
                .then(groupDetails => {
                    const groupTableBody = document.getElementById('created-groups-table-body');
                    groupTableBody.innerHTML = ''; // Xóa danh sách cũ

                    // Nếu không có nhóm
                    if (!groupDetails || groupDetails.length === 0) {
                        const noGroupsRow = document.createElement('tr');
                        noGroupsRow.innerHTML = '<td colspan="4" class="text-center">Người dùng chưa tạo nhóm nào</td>';
                        groupTableBody.appendChild(noGroupsRow);
                        return;
                    }

                    groupDetails.forEach((group, index) => {
                        if (group) {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <th scope="row">${index + 1}</th>
                                <td>${group.group_name}</td> <!-- Tên nhóm -->
                                <td>${group.admin_name}</td> <!-- Tên admin -->
                                <td>${group.member_count}</td> <!-- Số lượng thành viên -->
                            `;
                            groupTableBody.appendChild(row);
                        }
                    });
                })
                .catch(error => {
                    console.error('Lỗi khi lấy chi tiết nhóm:', error);
                });
        })
        .catch(error => {
            console.error('Lỗi khi lấy nhóm đã tạo của người dùng:', error);
        });
}



function getFriendsList(userId) {
    fetch(`http://localhost:8080/api/friend/get-all-friends/${userId}?status=APPROVED&page=0&size=100`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}` // Nếu cần xác thực
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh sách bạn bè: ${response.status}`);
            }
            return response.json();
        })
        .then(friendsData => {
            const friendsList = document.getElementById('friendsList');
            if (friendsData && friendsData.content && friendsData.content.length > 0) {
                friendsList.innerHTML = ''; // Xóa danh sách cũ
                friendsData.content.forEach((friend, index) => {
                    const row = document.createElement('tr');
                    const avatarContent = friend.fullname ? friend.fullname.charAt(0).toUpperCase() : '';
                    row.innerHTML = `
                    <th scope="row">${index + 1}</th>
                    <td class="text-center">
                        ${friend.avatarUrl ?
                            `<img src="${friend.avatarUrl}" alt="Avatar" class="img-fluid rounded-circle" style="width: 40px; height: 40px; background-color: #f0f0f0; color: #555; font-size: 32px; font-weight: bold; display: flex; justify-content: center; align-items: center;">` :
                            `<span class="d-flex justify-content-center align-items-center" style="background-color: #f0f0f0; color: #555; width: 40px; height: 40px; font-size: 20px; font-weight: bold; border-radius: 50%;">${avatarContent}</span>`
                        }
                    </td>
                    <td>${friend.fullname}</td>
                    <td>${friend.status || 'Đang trực tuyến'}</td>
                `;
                    friendsList.appendChild(row);
                });
            } else {
                friendsList.innerHTML = '<tr><td colspan="4" class="text-center">Không có bạn bè nào</td></tr>';
            }
        })
        .catch(error => {
            console.error('Lỗi khi lấy danh sách bạn bè:', error);
            document.getElementById('friendsList').innerHTML = '<tr><td colspan="4" class="text-center">Lỗi khi tải danh sách bạn bè</td></tr>';
        });
}



document.getElementById('addManager').addEventListener('click', function () {
    $('#addManagerModal').modal('show');  // Mở modal
    console.log("click");

});

document.getElementById("submitAddManagerButton").addEventListener("click", function () {
    // Vô hiệu hóa nút submit khi bắt đầu
    const submitButton = document.getElementById("submitAddManagerButton");
    submitButton.disabled = true;

    const fullname = document.getElementById('managerFullName').value.trim();
    const email = document.getElementById('managerEmail').value.trim();
    const phone = document.getElementById('managerPhone').value.trim();
    const password = document.getElementById('managerPassword').value.trim();

    if (!fullname || !email || !phone || !password) {
        showModal('danger', "Lỗi", "Vui lòng điền đầy đủ thông tin!");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    if (fullname.length < 3) {
        showModal('danger', "Lỗi", "Họ tên phải có ít nhất 3 ký tự.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    // Kiểm tra tên có chứa ký tự đặc biệt ngoài dấu cách không
    if (/[^A-Za-zÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƯ áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ\s]/.test(fullname)) {
        showModal('danger', "Lỗi", "Tên không được chứa ký tự đặc biệt ngoài dấu cách!");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const emailRegex = /^[a-zA-Z][a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]*(?:\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
        showModal('danger', "Lỗi", "Vui lòng nhập địa chỉ email hợp lệ.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const phoneRegex = /^(\\+84|84|0)(3|5|7|8|9|1[2689])[0-9]{8}$/;
    if (!phoneRegex.test(phone)) {
        showModal('danger', "Lỗi", "Vui lòng nhập số điện thoại hợp lệ.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const passwordRegexLowercase = /[a-z]/;
    const passwordRegexUppercase = /[A-Z]/;
    const passwordRegexNumber = /\d/;
    const passwordRegexSpecialChar = /[@$!%*?&]/;
    const passwordMinLength = 8;

    if (password.length < passwordMinLength) {
        showModal('danger', "Lỗi", "Mật khẩu phải có ít nhất 8 ký tự.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexLowercase.test(password)) {
        showModal('danger', "Lỗi", "Mật khẩu phải có ít nhất 1 chữ cái viết thường.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexUppercase.test(password)) {
        showModal('danger', "Lỗi", "Mật khẩu phải có ít nhất 1 chữ cái viết hoa.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexNumber.test(password)) {
        showModal('danger', "Lỗi", "Mật khẩu phải có ít nhất 1 chữ số.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }
    if (!passwordRegexSpecialChar.test(password)) {
        showModal('danger', "Lỗi", "Mật khẩu phải có ít nhất 1 ký tự đặc biệt.");
        submitButton.disabled = false;  // Kích hoạt lại nút submit
        return;
    }

    const managerData = {
        fullname: fullname,
        email: email,
        phoneNumber: phone,
        password: password
    };

    fetch('http://localhost:8080/onboarding/createManager', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify(managerData)
    })
        .then(response => response.json())
        .then(data => {
            console.log(data);

            if (data.status) {
                showModal('success', "Thông báo", "Thêm quản lý thành công!");
                var addManagerModal = new bootstrap.Modal(document.getElementById("addManagerModal"));
                addManagerModal.hide();

                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showModal('danger', "Lỗi", "Failed to create manager: " + data.error.message);
            }
            submitButton.disabled = false;
        })
        .catch(error => {
            console.error('Error:', error);
            showModal('danger', "Lỗi", 'An error occurred while creating the manager.');
            submitButton.disabled = false;  // Kích hoạt lại nút submit
        });
});


function checkStatus(userId) {
    const openButton = document.getElementById('openAccount');
    const lockButton = document.getElementById('confirmLock');
    const reasonInputLabel = document.getElementById("reasonInputLabel");
    const reasonInput = document.getElementById("reasonInput");
    const unlockConfirmationText = document.getElementById("unlockConfirmationText");

    return fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: AUTH_HEADER
    })
        .then(res => res.json())
        .then(data => {
            const userAccount = data.data.isLocked;

            if (userAccount === "LOCK") {
                document.getElementById("lockUserModalTitle").innerText = "Mở khóa tài khoản";
                lockButton.style.display = "none"; 
                openButton.style.display = "block";
                reasonInputLabel.style.display = "none"; 
                reasonInput.style.display = "none"; 
                unlockConfirmationText.style.display = "block"; 
            } else {
                document.getElementById("lockUserModalTitle").innerText = "Khóa tài khoản";
                openButton.style.display = "none"; 
                lockButton.style.display = "block";
                reasonInputLabel.style.display = "block"; 
                reasonInput.style.display = "block"; 
                unlockConfirmationText.style.display = "none"; 
            }
            return userAccount;
        })
        .catch(error => {
            console.error("Có lỗi xảy ra khi lấy thông tin người dùng:", error);
            alert("Có lỗi xảy ra khi lấy thông tin người dùng.");
            throw error;
        });
}




document.querySelectorAll('.btn-outline-success').forEach(button => {
    button.addEventListener('click', function () {
        const userId = this.getAttribute('data-id');

        const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
        modal.show();

        const openButton = document.getElementById('openAccount');
        const lockButton = document.getElementById('confirmLock');

        const newOpenButton = replaceButtonWithClone(openButton);
        const newLockButton = replaceButtonWithClone(lockButton);

        checkStatus(userId)
            .then(userAccount => {
                if (userAccount === "LOCK") {
                    newOpenButton.addEventListener("click", () => {
                        fetch(`${API_BASE_URL}/locked-forever/${userId}`, {
                            method: "PUT",
                            headers: {
                                'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                isLock: "OPEN",
                                reason: ""
                            })
                        })
                            .then(response => response.json())
                            .then(data => {
                                console.log(data);

                                if (data) {
                                    showNotification('success', 'Thành công', 'Mở tài khoản thành công');
                                    modal.hide();

                                    setTimeout(() => {
                                        window.location.reload();
                                    }, 1000);
                                } else {
                                    showNotification('error', 'Lỗi', 'Có lỗi xảy ra khi mở khóa tài khoản.');
                                }
                            })
                            .catch(error => console.error("Lỗi:", error));
                    });
                } else {
                    newLockButton.addEventListener("click", () => {
                        const reasonInput = document.getElementById("reasonInput").value.trim();

                        if (!reasonInput) {
                            showNotification('warning', 'Cảnh báo', 'Vui lòng nhập lý do khóa tài khoản.');
                            return; // Dừng việc gửi yêu cầu nếu lý do rỗng
                        }

                        fetch(`${API_BASE_URL}/locked-forever/${userId}`, {
                            method: "PUT",
                            headers: {
                                'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                isLock: "LOCK",
                                reason: reasonInput // Gửi lý do khóa người dùng
                            })
                        })
                            .then(response => response.json())
                            .then(data => {
                                console.log(data);

                                if (data) {
                                    showNotification('success', 'Thành công', 'Khóa tài khoản thành công');
                                    modal.hide();

                                    setTimeout(() => {
                                        window.location.reload();
                                    }, 1000);
                                } else {
                                    showNotification('error', 'Lỗi', 'Có lỗi xảy ra khi khóa tài khoản.');
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



//edit manager
const avatarUploader = document.getElementById('editAvatarUploaderModal');
const avatarImage = document.getElementById('editAvatarImgModal');
const avatarTextElement = document.getElementById('editAvatarTextModal');

// Lắng nghe sự kiện thay đổi file ảnh
avatarUploader.addEventListener('change', function (event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            avatarImage.src = e.target.result;
            avatarImage.style.display = 'block';
            avatarTextElement.style.display = 'none';
        };
        reader.readAsDataURL(file);
    } else {
        avatarImage.style.display = 'none';
        avatarTextElement.style.display = 'flex';
    }
});

// Xóa file ảnh khi người dùng click vào input file
avatarUploader.addEventListener('click', function () {
    avatarUploader.value = '';
});



document.querySelectorAll('.btn-outline-primary').forEach(button => {
    button.addEventListener('click', function () {
        const userId = this.getAttribute('data-id');

        const modal = new bootstrap.Modal(document.getElementById('editManagerModal'));
        modal.show();

        // Lấy thông tin người dùng và hiển thị lên modal
        fetch(`http://localhost:8080/onboarding/users/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
            .then(res => res.json())
            .then(data => {
                const avatarUrl = data.data.avatarUrl;
                const avatarElement = document.getElementById("editAvatarImgModal");
                const avatarTextElement = document.getElementById("editAvatarTextModal");

                if (avatarUrl) {
                    avatarElement.src = avatarUrl;
                    avatarElement.style.display = 'block';
                    avatarTextElement.style.display = 'none';
                } else {
                    const firstLetter = data.data.fullname ? data.data.fullname.charAt(0).toUpperCase() : 'N';
                    avatarTextElement.innerText = firstLetter;
                    avatarTextElement.style.display = 'flex';
                    avatarElement.style.display = 'none';
                }

                document.getElementById("editPhoneModal").value = data.data.phoneNumber;
                document.getElementById("editBirthDayModalLabel").value = data.data.dob;
                document.getElementById("editFullName").value = data.data.fullname;
                document.getElementById("editBioModalLabel").value = data.data.bio;

                document.getElementById('submit').setAttribute('data-user-id', userId);
                document.getElementById("changeFullnameButton").setAttribute('data-user-id', userId);
            });
    });
});

// Xử lý khi click nút "Lưu"
document.getElementById('submit').addEventListener('click', (e) => {
    e.preventDefault();

    const userId = e.target.getAttribute('data-user-id');
    saveChanges(userId);
});
function saveChanges(userId) {
    const phoneNumber = document.getElementById('editPhoneModal').value;
    const dob = document.getElementById('editBirthDayModalLabel').value;
    const bio = document.getElementById('editBioModalLabel').value;
    const avatarFile = document.getElementById('editAvatarUploaderModal').files[0];

    const phoneRegex = /^(\\+84|84|0)(3|5|7|8|9|1[2689])[0-9]{8}$/;

    if (!phoneRegex.test(phoneNumber)) {
        showModal('danger', "Lỗi", "Vui lòng nhập số điện thoại hợp lệ.");
        return;
    }

    const currentDate = new Date();
    const birthDate = new Date(dob);
    let age = currentDate.getFullYear() - birthDate.getFullYear();
    const monthDifference = currentDate.getMonth() - birthDate.getMonth();

    if (monthDifference < 0 || (monthDifference === 0 && currentDate.getDate() < birthDate.getDate())) {
        age--;
    }

    if (age < 18 || age > 100) {
        showModal('danger', "Lỗi", "Ngày sinh phải trong khoảng từ 18 đến 100 tuổi.");
        return;
    }

    const formData = new FormData();
    formData.append("updateDTO", new Blob([JSON.stringify({
        phoneNumber: phoneNumber,
        dob: dob,
        bio: bio
    })], { type: "application/json" }));

    if (avatarFile) {
        formData.append('avatar', avatarFile);
    }

    fetch(`http://localhost:8080/onboarding/change-profile/${userId}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log(data);

            if (data.status) {
                showModal('success', 'Thông báo', 'Cập nhật thông tin thành công!');
                const modal = new bootstrap.Modal(document.getElementById('editManagerModal'));
                modal.hide();
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showModal('danger', 'Lỗi', 'Cập nhật thất bại: ' + data.error.message);
            }
        })
        .catch(error => {
            console.error('Có lỗi xảy ra:', error);
        });
}



//change fullname
document.getElementById("changeFullnameButton").addEventListener("click", function () {
    const userId = this.getAttribute('data-user-id');

    const currentModal = document.getElementById("editManagerModal");
    const modalInstance = bootstrap.Modal.getInstance(currentModal);
    modalInstance.hide();

    const changeNameModal = new bootstrap.Modal(document.getElementById("changeFullnameModal"));
    changeNameModal.show();

});


document.getElementById("saveName").addEventListener("click", function (event) {
    event.preventDefault();

    const userId = document.getElementById("changeFullnameButton").getAttribute('data-user-id');
    const newFullname = document.getElementById("newFullName").value;

    // Hàm kiểm tra tên hợp lệ
    function validateFullname(name) {
        if (!name || name.trim() === "") {
            return "Tên không được để trống!";
        }

        const firstChar = name.charAt(0);
        if (!/^[A-Za-zÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƯáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ\s]/.test(firstChar)) {
            return "Tên phải bắt đầu bằng chữ cái!";
        }

        if (/[^A-Za-zÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƯáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ\s]/.test(name)) {
            return "Tên không được chứa ký tự đặc biệt ngoài dấu cách!";
        }

        // Kiểm tra độ dài của tên
        if (name.length < 3 || name.length > 100) {
            return "Tên phải có độ dài từ 3 đến 100 ký tự!";
        }

        return null; // Trả về null nếu tên hợp lệ
    }

    const validationError = validateFullname(newFullname);
    if (validationError) {
        showModal("Lỗi", validationError);
        return;
    }

    const encodedFullname = encodeURIComponent(newFullname);

    fetch(`http://localhost:8080/onboarding/change-full-name/${userId}?newName=${encodedFullname}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                showModal('Thông báo', 'Cập nhật tên thành công!');
                location.reload();  // Cập nhật lại trang nếu cần
            } else {
                showModal('Lỗi', 'Cập nhật tên thất bại: ' + (data.error?.message || 'Lỗi không xác định.'));
            }
        })
        .catch(error => {
            console.error('Có lỗi xảy ra:', error);
            showModal('Lỗi', 'Có lỗi xảy ra khi cập nhật tên. Vui lòng thử lại.');
        });
});

function showModal(type, title, message) {
    document.getElementById('notificationModalLabel').textContent = title;
    document.getElementById('notificationModalBody').textContent = message;

    const modalElement = document.getElementById('notificationModal');
    modalElement.classList.remove('modal-success', 'modal-info', 'modal-warning', 'modal-danger');

    if (type === 'success') {
        modalElement.classList.add('modal-success');
    } else if (type === 'info') {
        modalElement.classList.add('modal-info');
    } else if (type === 'warning') {
        modalElement.classList.add('modal-warning');
    } else if (type === 'danger') {
        modalElement.classList.add('modal-danger');
    }

    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}
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