# Review Zoo 운영 안내

현재는 **로컬 구현만 완료**했습니다. 원격 설정, push, 브랜치 생성, PR 생성은 하지 않았습니다.

## 한 번 세팅한 뒤 바꾸는 것

`scripts/review-zoo/config.json`에서 `stage`는 0=OT, 1~8=해당 주차입니다.
날짜로 주차를 추정하지 않습니다. `currentRound`는 1→2→3 순서로 바꿉니다.

```sh
node scripts/review-zoo/index.mjs --sync
```

설정 변경과 위 명령으로 생성한 README, `assets/review-zoo/state.json`,
`assets/review-zoo/world.svg`를 함께 본인의 과제 PR에 포함하면 됩니다.
`state.json`은 라운드 경계를 기억하는 세이브 파일이므로 삭제하지 마세요.

Round 1은 기존 리뷰 전체를 포함합니다. 다음 라운드는 **설정 변경 후 첫 성공한
`--sync` 시작 시각**부터 시작합니다. `currentRound`를 바꿀 때 바로 실행하세요.
댓글/리뷰 작성 시각으로 라운드를 구분하므로 예전 PR에 새로 단 댓글도 현재 라운드에
들어갑니다. 자동화만 이용하면 config가 main에 반영된 후 첫 갱신 시각이 경계입니다.
이전 라운드도 계속 표시하며, 원본에서 삭제된 댓글은 다음 조회에 반영됩니다.
라운드 건너뛰기/되돌리기는 거부합니다. 실수한 전환은 이전 세이브와 설정을 함께 복원하세요.

## 집계 규칙

- 지정된 저장소의 모든 열린/닫힌/병합된 PR 중 `SungKong00`이 작성한 PR만 조회합니다.
- 제출된 PR review 수와 inline review comment 수를 별도로 계산합니다.
- Inline 댓글 답글도 1개입니다. 일반 PR 대화 댓글(issue comment)은 제외합니다.
- 자기 리뷰, 봇 계정, 삭제된 사용자, 제출 전 pending review는 제외합니다.
- 리뷰만 남겨도 LV1. Inline comments 0~4=LV1, 5~9=LV2, 10+=LV3.
- 펫 종류는 GitHub 계정의 고정 숫자 ID로 결정합니다. 이름이 바뀌거나 라운드가 바뀌어도 같은 종류입니다.
- 모든 API 페이지를 조회합니다. API 실패 시 기존 출력은 덮어쓰지 않습니다.
- 저장하는 것은 공개 username/계정 ID/집계 수/경계 시각뿐이며 리뷰 본문이나 토큰은 저장하지 않습니다.
- README 첫 줄의 필수 배너와 생성 영역 밖의 수동 작성 내용은 보존합니다.

## 로컬에서 할 수 있는 것

Node.js 22 이상, GitHub CLI 로그인(`gh auth login`) 또는 `GH_TOKEN`이 필요합니다.
공개 저장소라도 인증을 사용해 API 한도를 확보합니다. 별도 패키지 설치는 없습니다.

```sh
# 인터넷 없이 저장된 데이터로 화면만 생성
node scripts/review-zoo/index.mjs
# GitHub 읽기 조회 + 로컬 파일 갱신 (원격 쓰기 없음)
node scripts/review-zoo/index.mjs --sync
# 동작 검증
node --test scripts/review-zoo/*.test.mjs
```

이 명령들은 어느 디렉터리에서 실행하든 스크립트가 속한 저장소를 대상으로 합니다.
일반적으로 저장소 루트에서 위 명령을 실행하세요. 과제 Java 코드는 변경하지 않습니다.
파일은 모두 임시 파일로 작성한 후 하나씩 교체합니다. 여러 파일 전체가 한 번에 바뀌는
트랜잭션은 아니므로 프로세스 강제 종료가 발생하면 같은 명령을 다시 실행하세요.

## 이번 PR에 한 번에 포함할 내용

README, 필수 배너, `assets/review-zoo/`, `scripts/review-zoo/`,
`.github/workflows/review-zoo.yml`, 이 운영 안내를 같은 PR에 포함합니다.
인증 토큰이나 개인 컴퓨터의 사용 기록은 PR에 넣지 않습니다.

## Actions: main에 병합하면 자동 시작

별도 서버, 개인 액세스 토큰, GitHub 변수의 최초 설정은 필요 없습니다.
**이 workflow가 main에 병합되면 첫 갱신을 실행하고, 이후 6시간마다 자동 저장합니다.**
원격에 아직 올리지 않은 현재 상태에서는 실행되지 않습니다.

- PR 단계: 테스트만 실행합니다. README를 원격에 저장하지 않습니다.
- main 병합 후: 작성자의 PR 리뷰를 읽고 README, SVG, state 세 파일을 갱신합니다.
- 바뀐 집계가 있으면 `github-actions[bot]`이 main에 세 파일을 commit/push합니다.
- 집계가 같으면 확인 시각을 유지하고 불필요한 commit을 만들지 않습니다.
- 새 브랜치나 PR은 자동으로 만들지 않습니다.
- 최초 workflow 병합 및 config/스크립트 변경 시 실행하며, 예약은 UTC
  00:23/06:23/12:23/18:23(KST 09:23/15:23/21:23/03:23)입니다.
- 개인 액세스 토큰 대신 GitHub가 실행마다 제공하는 `GITHUB_TOKEN`을 씁니다.
  리뷰 조회 작업에는 읽기 권한만, 저장 작업에만 `contents: write`를 부여합니다.

2026-09-27 확인 시 이 저장소는 Actions 사용이 허용되어 있고 main은 보호되지 않은
상태였습니다. 기본 workflow 권한은 읽기이며, 이 파일에서 저장 작업에만 쓰기를 요청합니다.
원격 설정을 변경한 것은 아닙니다. 실제 실행 성공 여부는 병합 후 확인해야 합니다.

## 병합 후 확인할 것

1. GitHub 저장소의 **Actions → Review Zoo**를 엽니다.
2. 병합 시 실행된 작업이 초록색이면 자동화가 완료된 것입니다.
3. 결과 화면의 `review-zoo-save`에서 생성 결과를 다운로드할 수도 있습니다.
4. 직접 시험하려면 **Run workflow**를 누릅니다. 기본은 미리보기만 만들고 main에 저장하지 않습니다.
   main에도 저장하려면 수동 실행 입력의 저장 체크박스를 선택합니다.

실패하면 Actions 로그를 확인합니다. 조직 정책이나 향후 추가한 main 보호 규칙이
쓰기 권한을 제한할 수 있습니다. 보호 규칙을 우회하거나 완화하지 않습니다.
생성 후 저장이 실패해도 artifact는 14일간 남습니다. README·SVG·state 세 파일을
함께 적용해 직접 PR로 제출할 수 있습니다.
main이 생성 도중 변경되면 덮어쓰지 않고 실패하므로 최신 main으로 다시 실행하세요.

자동 저장을 중단하려면 Settings → Secrets and variables → Actions → Variables에
`REVIEW_ZOO_AUTOPUBLISH` = `false`를 추가합니다. 다시 켜려면 변수를 삭제하거나
`true`로 바꿉니다. 중단 중에도 수동 미리보기와 PR 테스트는 가능합니다.
GitHub 예약 실행은 지연될 수 있고, 공개 저장소는 60일간 활동이 없으면 예약 실행이
비활성화될 수 있습니다. 필요한 경우 Actions에서 재활성화하세요.

## 렌더링과 출처

외부 폰트, JavaScript, 외부 이미지 참조 없이 SVG 사각형으로 픽셀을 표시합니다.
GitHub의 일반 README 이미지 방식이며 SVG 화면과 텍스트 표를 함께 제공합니다. 캐릭터에는 CSS로 작은 점프 모션을 넣었고, 모션 줄이기 설정에서는 정지합니다.
한국어는 뷰어의 기본 폰트로 표시합니다. GitHub 캐시 때문에 반영이 늦을 수 있습니다.

- [Tiny Creatures, Clint Bellanger (CC0)](https://opengameart.org/content/tiny-creatures)
- [원본 라이선스와 변환 기록](../assets/review-zoo/CREDITS.md)
- [GitHub Actions 이벤트/예약 실행](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows)
- [GITHUB_TOKEN 권한](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax#permissions)


## TokenPhage: 토큰 먹는 동료

2026-09-27 사용자 승인 후 TokenPhage CLI 2026.8.1을 이 Mac에 설치하고,
SungKong00 계정의 공개 Gist 인증과 매일 오전 4시 자동 동기화 등록을 완료했습니다.
README는 연동 대기 카드 대신 실제 계정 배지로 연결했습니다.

이 기능은 Review Zoo와 별개로 외부 TokenPhage 서비스에 의존합니다.
날짜·모델별 토큰 사용량 집계를 업로드합니다. 계정 인증 정보는 개인 컴퓨터의
`~/.tokenphage/config.json`에 소유자 전용 권한(0600)으로 저장되며 PR에 포함하지 않습니다.
과제 저장소에 개인 로그나 인증 정보를 복사하지 않았습니다.

- 수동 갱신: `tokenphage sync`
- 자동 갱신 중단: `tokenphage uninstall-hook`
- 자동 갱신 재등록: `tokenphage install-hook`
- 인증이 만료되면: `tokenphage login SungKong00`
- 동기화 기록: `~/.tokenphage/logs/sync.log`

자동 동기화는 이 Mac의 launchd 작업 `dev.tokenphage.sync`가 실행합니다.
이 Mac을 사용하지 않으면 계속 갱신되는 서버 작업이 아닙니다. 컴퓨터가 꺼져 있거나
인터넷 연결이 없을 때는 갱신되지 않을 수 있으므로 필요 시 수동 갱신하세요.
Review Zoo의 GitHub Actions에는 토큰 수집이나 인증 정보가 들어가지 않습니다.

[공식 CLI 안내](https://github.com/TOKENPHAGE/tokenphage-cli) ·
[실제 배지](https://api.tokenphage.com/badge/SungKong00?theme=gpu&mode=dark)

배지는 서비스와 GitHub 캐시 때문에 최신 수치 반영에 시간이 걸릴 수 있습니다.
TokenPhage 영역은 생성 마커 밖에 있으므로 Review Zoo 갱신 시에도 유지됩니다.


## 저장소 구조와 Git 추적

```text
assignment/
├── .github/                 # 과제 템플릿, GitHub Actions
├── gradle/wrapper/          # 공유 Gradle 실행 도구 (JAR 포함)
├── src/                    # 과제 Java 소스와 테스트
├── scripts/review-zoo/      # 리뷰 수집·집계·화면 생성·테스트·설정
├── assets/review-zoo/       # 픽셀 리소스, 라이선스, 생성 화면, 라운드 세이브
├── docs/                   # 운영 안내
├── README.md               # 필수 배너 + 자동 생성 화면 + TokenPhage
├── banner_server.png       # 최상단 필수 배너
├── build.gradle            # 공용 빌드 설정
├── settings.gradle         # 프로젝트 설정
├── gradlew / gradlew.bat    # OS별 Gradle 실행 진입점
└── .gitignore              # 빌드·IDE·OS·로컬 설정·임시 파일 제외 규칙
```

현재 프로젝트에는 Java 플러그인만 있으며 Spring Boot는 아직 적용하지 않았습니다.
`.gitignore`는 향후 Spring Boot의 개인용 `application-local.*` 설정을 제외하도록
준비하되, 공용 `application.yml`, 공유 프로필, 예제 환경 파일은 포함합니다.
특정 Spring Boot 설정을 필수로 만들거나 의존성을 추가하지 않았습니다.

기존 추적 대상이던 `.idea` 파일 6개는 로컬 파일을 보존하고 Git 추적만 해제했습니다.
현재 공유 코드 스타일 설정이 없고 프로젝트 구성이 Gradle에 있으므로 IDE 메타데이터를
저장소에서 제외합니다. 팀 공용 IDE 규칙이 필요해지면 해당 파일만 명시적으로 포함하세요.
Git에서 보이는 이 6개의 삭제 항목은 다음 커밋에 포함해야 추적 해제가 원격에도 반영됩니다.

생성 파일이라고 모두 제외하지 않습니다. `world.svg`와 `state.json`은 README 표시와
라운드 유지에 필요하므로 반드시 포함합니다. `.env`는 제외하지만 `.env.example`,
`.env.sample`, `.env.template`과 환경별 예제는 공유할 수 있습니다.

참고: [GitHub Gradle ignore 템플릿](https://github.com/github/gitignore/blob/main/Gradle.gitignore),
[JetBrains](https://github.com/github/gitignore/blob/main/Global/JetBrains.gitignore),
[macOS](https://github.com/github/gitignore/blob/main/Global/macOS.gitignore).
